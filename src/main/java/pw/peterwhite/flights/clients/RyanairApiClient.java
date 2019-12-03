package pw.peterwhite.flights.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import pw.peterwhite.flights.config.FlightConfigProperties;
import pw.peterwhite.flights.dto.Journey.Leg;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;
import pw.peterwhite.flights.dto.Schedule.DayFlights;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
public class RyanairApiClient extends Client {
    private static final Log logger = LogFactory.getLog(RyanairApiClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public RyanairApiClient(FlightConfigProperties flightConfigProperties) {
        this.baseUrl = flightConfigProperties.getRyanairApiClientBaseUrl();
    }

    /**
     * <b>getRoutes</b>:
     *
     * @return List of all routes from API
     */
    public List<Route> getRoutes() {
        final URI routesApi = URI.create(baseUrl + "/locate/3/routes/");
        logger.info("Making request to Routes API: " + routesApi);
        ResponseEntity<List<Route>> result;
        try {
            // Make call of Routes API using restTemplate.exchange(url, httpMethod, requestEntity, responseType)
            result = restTemplate.exchange(routesApi,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Route>>() {
                    });
        } catch (HttpClientErrorException ex) {
            switch (ex.getRawStatusCode()) {
                case 400:
                    logger.info("Bad params supplied to Routes API");
                    break;
                case 404:
                    logger.info("No data found from Routes API");
                    break;
            }
            return Collections.emptyList();
        } catch (HttpServerErrorException ex) {
            logger.error("Routes API Server error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream Routes API error");
        } catch (UnknownHttpStatusCodeException ex) {
            logger.error("Routes API returned unknown HTTP status: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream Routes API error");
        } catch (Exception ex) {
            logger.error("Routes API Response error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream Routes API error");
        }

        if (result == null) {
            return Collections.emptyList();
        }

        List<Route> routes = result.getBody();
        if (routes != null) {
            routes.removeIf(Objects::isNull);
        }

        return routes;
    }

    public List<Leg> getSchedules(Route route, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        //logger.info(String.format("Querying schedules for Route %s-%s", route.airportFrom, route.airportTo));
        List<Leg> availableLegs = new ArrayList<>();

        String airportFrom = route.getAirportFrom();
        String airportTo = route.getAirportTo();
        if (airportFrom == null || airportTo == null) {
            logger.warn("Malformed route");
            return Collections.emptyList();
        }

        // For each month, get the schedules for our Route. Flatten the response to a dto.Journey.Leg format and
        // filter to those which fit within our departure-arrival date-times.
        for (LocalDateTime dateTime = departureDateTime;
             dateTime.getYear() != arrivalDateTime.getYear() && dateTime.getMonthValue() <= arrivalDateTime.getMonthValue();
             dateTime = dateTime.plusMonths(1)) {

            int year = dateTime.getYear();
            int month = dateTime.getMonthValue();

            String apiPath = String.format("/timtbl/3/schedules/%s/%s/years/%s/months/%s", airportFrom, airportTo, year, month);
            URI schedulesApi = URI.create(baseUrl + apiPath);

            logger.info("Making request to Schedules API: " + schedulesApi);

            ResponseEntity<Schedule> result;
            try {
                // Make call Schedules API using restTemplate.exchange(url, httpMethod, requestEntity, responseType)
                result = restTemplate.exchange(schedulesApi,
                        HttpMethod.GET,
                        null,
                        Schedule.class);
            } catch (HttpClientErrorException ex) {
                switch (ex.getRawStatusCode()) {
                    case 400:
                        logger.info("Bad params supplied to Schedules API: " + ex.getMessage());
                        break;
                    case 404:
                        logger.info("No data found from Schedules API");
                        break;
                    case 429:
                        // Should implement retry mechanism for this, but for now, just throw a 500 error
                        logger.info("Rate-limited by Schedules API");
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server has been rate-limited by Upstream API");
                    default:
                        logger.warn("Schedules API returned an unexpected 4xx");
                }
                continue;
            } catch (HttpServerErrorException ex) {
                logger.error("Schedules API Response error: " + ex.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream Schedules API error");
            } catch (HttpStatusCodeException ex) {
                continue;
            }

            Schedule schedule = result.getBody();
            if (schedule == null) {
                continue;
            }

            List<Leg> schedules = flattenSchedule(airportFrom, airportTo, year, schedule);

            // Flight departure is at or after our specified departure date-time ie
            Predicate<Leg> departurePredicate = flight -> flight.getDepartureTime().isAfter(departureDateTime) ||
                    flight.getDepartureTime().isEqual(departureDateTime);

            // Flight arrival is at or after our specified arrival date-time
            Predicate<Leg> arrivalPredicate = flight -> flight.getArrivalTime().isBefore(arrivalDateTime) ||
                    flight.getArrivalTime().isEqual(arrivalDateTime);

            // In the case of bad data from the Schedules API, check to ensure all departures are before their arrivals
            Predicate<Leg> departureBeforeArrivalPredicate = flight -> flight.getDepartureTime().isBefore(flight.getArrivalTime());

            List<Leg> filteredSchedules = schedules.stream()
                    .filter(departurePredicate.and(arrivalPredicate).and(departureBeforeArrivalPredicate))
                    .collect(Collectors.toList());

            availableLegs.addAll(filteredSchedules);
        }
        return availableLegs;
    }

    private List<Leg> flattenSchedule(String airportFrom, String airportTo, int year, Schedule schedule) {
        // Response from Schedules API is messy with month, day, and times all separated and no year.
        // Flatten it to dto.Journey.Leg format with LocalDateTime for the departure and arrival date-times.
        // Also have access to the route information at this scope, so include it now.
        List<Leg> legs = new ArrayList<>();

        int month = schedule.getMonth();

        for (DayFlights flightsDay : schedule.getDays()) {
            LocalDate date = LocalDate.of(year, month, flightsDay.getDay());

            for (Schedule.FlightDetails details : flightsDay.getFlights()) {
                LocalTime departureTime = LocalTime.parse(details.getDepartureTime());
                LocalTime arrivalTime = LocalTime.parse(details.getArrivalTime());

                legs.add(new Leg(airportFrom, airportTo,
                        LocalDateTime.of(date, departureTime), LocalDateTime.of(date, arrivalTime)));
            }
        }
        return legs;
    }
}