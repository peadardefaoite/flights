package pw.peterwhite.flights.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.dto.Flight;
import pw.peterwhite.flights.dto.Flight.Leg;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlightService {
    private static final Log logger = LogFactory.getLog(FlightService.class);

    @Autowired
    private RyanairApiClient ryanairApiClient;

    /* Requirements for finding suitable routes between departure airport and arrival airport:
        - connectingAirport is null
        - operator is "RYANAIR"
        - in the case of zero stops: airportFrom matches departure airport AND airportTo matches arrival airport
        - in the case of one stop: airportFrom matches departure airport OR airportTo matches arrival airport, this will
            result in two lists, and we need to filter those based on the airportTo of departure airport matching the
            airportFrom of the arrival airport.
    */
    public List<Flight> getAvailableFlights(String departure,
                                            String arrival,
                                            LocalDateTime departureDateTime,
                                            LocalDateTime arrivalDateTime) {
        logger.info(String.format("Getting flights from %s-%s between %s and %s", departure, arrival, departureDateTime, arrivalDateTime));

        logger.info("Making request to Routes API");
        List<Route> routes = ryanairApiClient.getRoutes();
        logger.info("Total routes: " + routes.size());

        routes.removeIf(route -> route.connectingAirport != null || !Objects.equals(route.operator, "RYANAIR"));

        if (routes.size() == 0) {
            logger.info("No available routes");
            return Collections.emptyList();
        }

        Predicate<Route> isDirectFlight = route -> departure.equals(route.airportFrom) && arrival.equals(route.airportTo);
        List<Route> directRoutes = routes.stream().filter(isDirectFlight).collect(Collectors.toList());

        logger.info("Direct flights: " + directRoutes.size());

        if (directRoutes.size() > 1) {
            logger.error("Error in Routes API. Returned more than one direct route for " + departure + "-" + arrival);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Routes API returned more than one direct route");
        }
        List<Flight> flightList = new ArrayList<>();

        Predicate<Route> isDepartureRoute = route -> departure.equals(route.airportFrom);

        List<Route> departureRoutes = routes.stream().filter(isDepartureRoute).collect(Collectors.toList());

        Predicate<Route> isArrivalRoute = route -> arrival.equals(route.airportTo);
        List<Route> arrivalRoutes = routes.stream().filter(isArrivalRoute).collect(Collectors.toList());

        // Remove route from both Lists if the intermediate airport doesn't match, that is,
        // any(airportTo) of departureRoutes != any(airportFrom) of arrivalRoutes
        departureRoutes.removeIf(departureRoute -> arrivalRoutes.stream()
                .noneMatch(arrivalRoute -> Objects.equals(arrivalRoute.airportFrom, departureRoute.airportTo)));
        arrivalRoutes.removeIf(arrivalRoute -> departureRoutes.stream()
                .noneMatch(departureRoute -> Objects.equals(departureRoute.airportTo, arrivalRoute.airportFrom)));

        if (departureRoutes.size() != arrivalRoutes.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Routes mismatch");
        }

        logger.info("Available indirect flights: " + departureRoutes.size());

        // Sort our ArrayLists so that the departureRoutes[i].airportTo == arrivalRoutes[i].airportFrom
        departureRoutes.sort(Comparator.comparing(route -> route.airportTo));
        arrivalRoutes.sort(Comparator.comparing(route -> route.airportFrom));

        List<Schedule> directSchedules = ryanairApiClient.getSchedules(directRoutes, departureDateTime, arrivalDateTime);


        // TODO: Need schedules below

        if (directRoutes.size() == 1) {
            Route directRoute = directRoutes.get(0);
            Leg directLeg = new Leg(directRoute.airportFrom, directRoute.airportTo, null, null);
            List<Leg> legs = Collections.singletonList(directLeg);
            flightList.add(new Flight(0, legs));
        }

        Iterator<Route> departureIterator = departureRoutes.iterator();
        Iterator<Route> arrivalIterator = arrivalRoutes.iterator();
        while (departureIterator.hasNext() && arrivalIterator.hasNext()) {
            Route departureRoute = departureIterator.next();
            Route arrivalRoute = arrivalIterator.next();

            Leg departureLeg = new Leg(departureRoute.airportFrom, departureRoute.airportTo, null, null);
            Leg arrivalLeg = new Leg(arrivalRoute.airportFrom, arrivalRoute.airportTo, null, null);
            List<Leg> legs = Arrays.asList(departureLeg, arrivalLeg);
            flightList.add(new Flight(1, legs));
        }

        return flightList;
    }
}
