package pw.peterwhite.flights.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.config.FlightConfigProperties;
import pw.peterwhite.flights.dto.Journey;
import pw.peterwhite.flights.dto.Journey.Leg;
import pw.peterwhite.flights.dto.Route;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlightService {
    private static final Log logger = LogFactory.getLog(FlightService.class);

    @Autowired
    private FlightConfigProperties flightConfigProperties;

    @Autowired
    private RyanairApiClient ryanairApiClient;

    /* Requirements for finding suitable routes between departure airport and arrival airport:
        - connectingAirport is null
        - operator is "RYANAIR" (read from config)
        - in the case of zero stops: airportFrom matches departure airport AND airportTo matches arrival airport
        - in the case of one stop: airportFrom matches departure airport OR airportTo matches arrival airport, this will
            result in two lists, and we need to filter those based on the airportTo of departure airport matching the
            airportFrom of the arrival airport.
    */
    public List<Journey> getAvailableFlights(String departure,
                                             String arrival,
                                             LocalDateTime departureDateTime,
                                             LocalDateTime arrivalDateTime) {
        if (departure == null || arrival == null || departureDateTime == null || arrivalDateTime == null) {
            logger.error("Invalid parameters supplied to FlightService.getAvailableFlights");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info(String.format("Getting flights from %s-%s between %s and %s", departure, arrival, departureDateTime, arrivalDateTime));

        List<Route> allRoutes = ryanairApiClient.getRoutes();
        logger.info("Total routes: " + allRoutes.size());

        String routeOperator = flightConfigProperties.getRouteOperator();
        logger.info("Removing routes that have a non-null connectingAirport and the operator is not '" + routeOperator + "'");
        allRoutes.removeIf(route -> route.getConnectingAirport() != null || !Objects.equals(route.getOperator(), routeOperator));

        if (allRoutes.size() == 0) {
            logger.info("No available routes");
            return Collections.emptyList();
        }

        // List to hold all suitable journeys
        List<Journey> journeyList = new ArrayList<>();

        // Direct flights
        Predicate<Route> isDirectFlight = route -> departure.equals(route.getAirportFrom()) && arrival.equals(route.getAirportTo());
        List<Route> directRoutes = allRoutes.stream().filter(isDirectFlight).collect(Collectors.toList());

        if (directRoutes.size() > 1) {
            logger.error("Error in Routes API. Returned more than one direct route for " + departure + "-" + arrival);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Routes API returned more than one direct route");
        } else if (directRoutes.size() == 1) {
            logger.info("Direct route found");
            List<Leg> directFlights = ryanairApiClient.getSchedules(directRoutes.get(0), departureDateTime, arrivalDateTime);
            for (Leg flights : directFlights) {
                journeyList.add(new Journey(0, Collections.singletonList(flights)));
            }
        } else {
            logger.info("No direct route found");
        }
        // End Direct flights

        // Journeys with 1 stop
        Predicate<Route> isDepartureRoute = route -> departure.equals(route.getAirportFrom());
        List<Route> departureRoutes = allRoutes.stream().filter(isDepartureRoute).collect(Collectors.toList());

        Predicate<Route> isArrivalRoute = route -> arrival.equals(route.getAirportTo());
        List<Route> arrivalRoutes = allRoutes.stream().filter(isArrivalRoute).collect(Collectors.toList());

        // Remove route from both Lists if the intermediate airport doesn't match, that is,
        // (airportTo) of departureRoutes != any(airportFrom) of arrivalRoutes and vice versa
        departureRoutes.removeIf(departureRoute -> arrivalRoutes.stream()
                .noneMatch(arrivalRoute -> Objects.equals(arrivalRoute.getAirportFrom(), departureRoute.getAirportTo())));
        arrivalRoutes.removeIf(arrivalRoute -> departureRoutes.stream()
                .noneMatch(departureRoute -> Objects.equals(departureRoute.getAirportTo(), arrivalRoute.getAirportFrom())));

        if (departureRoutes.size() != arrivalRoutes.size()) {
            // Expect no duplicate Routes that would cause this
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Bad data from the Routes API");
        }

        logger.info("Available indirect flights: " + departureRoutes.size());

        // Sort our ArrayLists so that departureRoutes[i].airportTo == arrivalRoutes[i].airportFrom
        departureRoutes.sort(Comparator.comparing(Route::getAirportTo));
        arrivalRoutes.sort(Comparator.comparing(Route::getAirportFrom));

        Iterator<Route> departureIterator = departureRoutes.iterator();
        Iterator<Route> arrivalIterator = arrivalRoutes.iterator();
        while (departureIterator.hasNext() && arrivalIterator.hasNext()) {
            Route departureRoute = departureIterator.next();
            Route arrivalRoute = arrivalIterator.next();

            List<Leg> departureLegs = ryanairApiClient.getSchedules(departureRoute, departureDateTime, arrivalDateTime);
            if (departureLegs.isEmpty()) {
                // No suitable departures found, no point checking for the arrival legs
                logger.info("No first legs found for " + departureRoute.getAirportFrom() + "-" + departureRoute.getAirportTo());
                continue;
            }

            List<Leg> arrivalLegs = ryanairApiClient.getSchedules(arrivalRoute, departureDateTime, arrivalDateTime);
            if (arrivalLegs.isEmpty()) {
                // No suitable arrivals found.
                logger.info("No second legs found for " + arrivalRoute.getAirportFrom() + "-" + arrivalRoute.getAirportTo());
                continue;
            }

            for (Leg firstLeg : departureLegs) {
                for (Leg secondLeg : arrivalLegs) {
                    // 2nd leg departure time from intermediate airport cannot be before 2 hours after 1st leg arrival time
                    if (!secondLeg.getDepartureTime().isBefore(firstLeg.getArrivalTime().plusHours(2))) {
                        journeyList.add(new Journey(1, Arrays.asList(firstLeg, secondLeg)));
                    }
                }
            }
        }
        logger.info("Total valid journeys found: " + journeyList.size());
        return journeyList;
    }
}
