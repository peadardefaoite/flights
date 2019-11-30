package pw.peterwhite.flights.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.peterwhite.flights.dto.Flight;
import pw.peterwhite.flights.dto.Leg;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightService implements Service {
    private static final Log logger = LogFactory.getLog(FlightService.class);

    public List<Flight> getAvailableFlights(String departure,
                                            String arrival,
                                            LocalDateTime departureDateTime,
                                            LocalDateTime arrivalDateTime) {
        List<Flight> flightList = new ArrayList<>();

        Leg leg = new Leg(departure, arrival, departureDateTime, arrivalDateTime);
        List<Leg> legs = new ArrayList<>();
        legs.add(leg);

        flightList.add(new Flight(0, legs));

        return flightList;
    }
}
