package pw.peterwhite.flights.helpers;

import pw.peterwhite.flights.dto.Flight;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TestHelper class for common methods used across various tests
 */
public class TestHelper {
    static public List<Flight> generateFlightList(String departure, String arrival,
                                            LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        Flight flight = new Flight();
        Flight.Leg leg = new Flight.Leg();
        leg.departureAirport = departure;
        leg.arrivalAirport = arrival;
        leg.departureTime = departureDateTime;
        leg.arrivalTime = arrivalDateTime;

        flight.legs = new ArrayList<>();
        flight.legs.add(leg);
        flight.stops = flight.legs.size();

        List<Flight> flightList = new ArrayList<>();
        flightList.add(flight);
        return flightList;
    }

    static public List<Route> generateRouteList() {
        return new ArrayList<>();
    }

    static public List<Schedule> generateScheduleList() {
        return new ArrayList<>();
    }
}
