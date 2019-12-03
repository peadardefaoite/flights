package pw.peterwhite.flights.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
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
        } catch (RestClientException ex) {
            logger.error("Routes API Client error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred when communicating with the Routes API");
        } catch (Exception ex) {
            logger.error("Routes API Response error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred with the response from the Routes API");
        }
        checkResponse(result);

        List<Route> routes = result.getBody();
        if (routes != null) {
            routes.removeIf(Objects::isNull);
        }

        return routes;
    }

    public List<Leg> getSchedules(Route route, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        logger.info(String.format("Querying schedules for Route %s-%s", route.airportFrom, route.airportTo));
        List<Leg> availableLegs = new ArrayList<>();

        // For each month, get the schedules for our Route. Flatten the response to a dto.Journey.Leg format and
        // filter to those which fit within our departure-arrival date-times.
        for (LocalDateTime dateTime = departureDateTime;
             dateTime.getMonthValue() <= arrivalDateTime.getMonthValue();
             dateTime = dateTime.plusMonths(1)) {

            int year = dateTime.getYear();
            int month = dateTime.getMonthValue();

            String apiPath = String.format("/timtbl/3/schedules/%s/%s/years/%s/months/%s", route.airportFrom, route.airportTo, year, month);
            URI schedulesApi = URI.create(baseUrl + apiPath);

            logger.info("Making request to Schedules API: " + schedulesApi);

            ResponseEntity<Schedule> result;
            try {
                // Make call Schedules API using restTemplate.exchange(url, httpMethod, requestEntity, responseType)
                result = restTemplate.exchange(schedulesApi,
                        HttpMethod.GET,
                        null,
                        Schedule.class);
            } catch (RestClientException ex) {
                logger.error("Schedules API Client error: " + ex.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred when communicating with the Schedules API");
            } catch (Exception ex) {
                logger.error("Schedules API Response error: " + ex.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred with the response from the Schedules API");
            }

            checkResponse(result);

            Schedule schedule = result.getBody();
            if (schedule == null) {
                continue;
            }

            List<Leg> schedules = flattenSchedule(route, year, schedule);

            // Flight departure is at or after our specified departure date-time
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

    private List<Leg> flattenSchedule(Route route, int year, Schedule schedule) {
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

                legs.add(new Leg(route.airportFrom, route.airportTo,
                        LocalDateTime.of(date, departureTime), LocalDateTime.of(date, arrivalTime)));
            }
        }
        return legs;
    }

    // Helper method for checking if Response from API is ok
    private void checkResponse(ResponseEntity result) throws ResponseStatusException {
        if (result == null) {
            logger.error("Null response from upstream API");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in communicating with upstream API");
        }

        HttpStatus statusCode = result.getStatusCode();

        if (statusCode.is4xxClientError()) {
            logger.error("Error in information supplied to Upstream API");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in information supplied to Upstream API");
        } else if (statusCode.is5xxServerError()) {
            logger.warn("Error in Upstream API: " + statusCode.getReasonPhrase());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error in upstream API");
        }
    }
}