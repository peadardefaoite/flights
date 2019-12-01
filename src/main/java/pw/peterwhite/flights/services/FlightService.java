package pw.peterwhite.flights.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.dto.Flight;
import pw.peterwhite.flights.dto.Flight.Leg;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightService {
    private static final Log logger = LogFactory.getLog(FlightService.class);

    @Autowired
    private RyanairApiClient ryanairApiClient;

    public List<Flight> getAvailableFlights(String departure,
                                            String arrival,
                                            LocalDateTime departureDateTime,
                                            LocalDateTime arrivalDateTime) {
        List<Route> routes = ryanairApiClient.getRoutes();
        List<Schedule> schedules = ryanairApiClient.getSchedules();

        //TODO: proper logic to use routes and schedules to determine available flights
        Flight flight = new Flight();
        Leg leg = new Leg();

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
}
