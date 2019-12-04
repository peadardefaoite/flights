package pw.peterwhite.flights.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RyanairApiClient extends Client {
    private static final Log logger = LogFactory.getLog(RyanairApiClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public RyanairApiClient(FlightConfigProperties flightConfigProperties) {
        this.baseUrl = flightConfigProperties.getRyanairApiClientBaseUrl();
        logger.info("RyanairApiClient instantiated with baseUrl: " + baseUrl);
    }

    /**
     * <b>getRoutes</b>: This method crafts a URI to the Routes API baseUrl/locate/3/routes/ and performs a HTTP GET to
     * the API. It transforms the response into List\<Route\> and removes any null entries.
     *
     * @return List of all routes from external API. Can be empty if none found (404 from Routes API)
     * @throws ResponseStatusException:
     *          * 500 if the API returns a 429 Too Many Requests (rate-limited)
     *          * 500 if unexpected error occurs from the request to external API (such as UnknownHostException)
     *          * 502 if the API returns a 5xx or unknown status code
     */
    public List<Route> getRoutes() {
        String routesApiPath = "/locate/3/routes/";
        final URI routesApi = URI.create(baseUrl + routesApiPath);
        logger.info("Making request to Routes API: " + routesApiPath);
        ResponseEntity<List<Route>> result;
        try {
            // Make call to Routes API using restTemplate.exchange(url, httpMethod, requestEntity, responseType)
            result = restTemplate.exchange(routesApi,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Route>>() {
                    });
        } catch (HttpClientErrorException ex) {
            // API returned 4xx
            switch (ex.getRawStatusCode()) {
                case 400:
                    logger.info("Bad params supplied to Routes API");
                    break;
                case 404:
                    logger.info("No data found from Routes API");
                    break;
                case 429:
                    // Should implement retry mechanism for this, but for now, just throw a 500 error
                    logger.info("Rate-limited by Routes API");
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server has been rate-limited by Upstream API");
                default:
                    logger.warn("Routes API returned an unexpected 4xx: " + ex.getMessage());
            }
            return Collections.emptyList();
        } catch (HttpServerErrorException ex) {
            // API returned 5xx
            logger.error("Routes API Server error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream Routes API error");
        } catch (UnknownHttpStatusCodeException ex) {
            // API returned unknown status code
            logger.error("Routes API returned unknown HTTP status: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream Routes API error");
        } catch (Exception ex) {
            // All other errors eg UnknownHostException. Code reachable if the network is down, cannot resolve baseURL host, etc
            logger.error("Unexpected error when communicating with Routes API: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upstream Routes API error");
        }

        List<Route> routes = result.getBody();
        if (routes != null) {
            routes.removeIf(Objects::isNull);
        }
        return routes;
    }

    /**
     * <b>getSchedules</b>: This method crafts a URI to the Schedules API - baseUrl/timtbl/3/{departure}/{arrival}/years/{year}/months/{month}
     * It does this for the given Route multiple times for each year and month in the given time-frame, and collects them into a Schedule object.
     * It flattens this structure to a Leg DTO and filters out the flight date-times that are not in the specified time range.
     * If no Schedule is found for a given route/year/month (404 from the API), it simply continues to the next month.
     *
     * @param route: The route to check. Consists of departure airport and arrival airport in IATA code format
     * @param departureDateTime: departure date-time in the timezone of departure airport
     * @param arrivalDateTime: arrival date-time in the timezone of arrival airport
     * @return List of all available flights in the specified time range for the given route. Represented as a Leg (see DTO for details)
     * @throws  ResponseStatusException:
     *          * 500 if the API returns a 429 Too Many Requests (rate-limited)
     *          * 500 if unexpected error occurs from the request to external API (such as UnknownHostException)
     *          * 502 if the API returns a 5xx or unknown status code
     */
    public List<Leg> getSchedules(Route route, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        String airportFrom = route.getAirportFrom();
        String airportTo = route.getAirportTo();
        if (airportFrom == null || airportTo == null) {
            logger.warn("Malformed route");
            return Collections.emptyList();
        }

        List<Leg> availableLegs = new ArrayList<>();

        String scheduleApiPath = "/timtbl/3/schedules/%s/%s/years/%s/months/%s";

        // For each month, get the schedules for our Route. Flatten the response to a dto.Journey.Leg format and
        // filter to those which fit within our departure-arrival date-times.
        // Assumption is made here that there are no departing flights that arrive the day before in a
        // different timezone. Edge case scenario could cause this loop to fail where a flight departs on 12:01AM on 1/1/2020
        // but arrives at 11:59PM on 31/12/2019. All flights must depart and land on the same day in their local times.
        for (LocalDateTime dateTime = departureDateTime;
             !YearMonth.from(dateTime).isAfter(YearMonth.from(arrivalDateTime));
             dateTime = dateTime.plusMonths(1)) {

            int year = dateTime.getYear();
            int month = dateTime.getMonthValue();

            String apiPath = String.format(scheduleApiPath, airportFrom, airportTo, year, month);
            URI schedulesApi = URI.create(baseUrl + apiPath);

            logger.info("Making request to Schedules API: " + apiPath);

            ResponseEntity<Schedule> result;
            try {
                // Make call to Schedules API using restTemplate.exchange(url, httpMethod, requestEntity, responseType)
                result = restTemplate.exchange(schedulesApi,
                        HttpMethod.GET,
                        null,
                        Schedule.class);
            } catch (HttpClientErrorException ex) {
                // API returned 4xx
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
                        logger.warn("Schedules API returned an unexpected 4xx: " + ex.getMessage());
                }
                continue;
            } catch (HttpServerErrorException ex) {
                // API returned 5xx. Return to user instead of continuing as may return incomplete results.
                logger.error("Schedules API Response error: " + ex.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream Schedules API error");
            } catch (UnknownHttpStatusCodeException ex) {
                logger.error("Schedules API returned unknown HTTP status: " + ex.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream Schedules API error");
            } catch (Exception ex) {
                // All other errors eg UnknownHostException. Code reachable if the network is down, cannot resolve baseURL host, etc
                logger.error("Unexpected error when communicating with Schedules API: " + ex.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upstream Schedules API error");
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

            List<Leg> filteredSchedules = schedules.stream()
                    .filter(departurePredicate.and(arrivalPredicate))
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

                legs.add(new Leg(airportFrom,
                        airportTo,
                        LocalDateTime.of(date, departureTime),
                        LocalDateTime.of(date, arrivalTime)
                ));
            }
        }
        return legs;
    }
}