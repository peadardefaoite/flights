package pw.peterwhite.flights.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pw.peterwhite.flights.dto.Flight;
import pw.peterwhite.flights.dto.Leg;
import pw.peterwhite.flights.services.FlightService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1")
public class FlightV1Controller {

    private static final Log logger = LogFactory.getLog(FlightV1Controller.class);
    private FlightService flightService;

    @Autowired
    public FlightV1Controller(FlightService flightService) {
        this.flightService = flightService;
    }

    @RequestMapping("hello")
    public String hello() {
        int flightInfo = flightService.getFlightInfo();
        logger.info("Received request");
        logger.info("Flight info: " + flightInfo);

        return "Greetings from Spring Boot!";
    }

    @RequestMapping(path = "/interconnections",
            method = GET,
            params = {"departure", "arrival", "departureDateTime", "arrivalDateTime"})
    public List<Flight> interconnections(String departure,
                                         String arrival,
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDateTime,
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateTime) {
        validateParams(departure, arrival, departureDateTime, arrivalDateTime);

        //int flightInfo = flightService.getFlightInfo();
        logger.info("Received request");

        List<Flight> flightList = new ArrayList<>();

        Leg leg = new Leg(departure, arrival, departureDateTime, arrivalDateTime);
        List<Leg> legs = new ArrayList<>();
        legs.add(leg);

        flightList.add(new Flight(0, legs));
        return flightList;
    }

    private void validateParams(String departure,
                                String arrival,
                                LocalDateTime departureDateTime,
                                LocalDateTime arrivalDateTime) {
        if (departure == null || !departure.matches("^[A-Z]{3}$")
                || arrival == null || !arrival.matches("^[A-Z]{3}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid departure or arrival codes");
        }

        if (departure.equals(arrival)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure and arrival codes are the same");
        }

        if (departureDateTime == null || arrivalDateTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No date-time given for departure or arrival");
        }

        if (arrivalDateTime.isBefore(departureDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arrival time is before departure time");
        }
    }
}
