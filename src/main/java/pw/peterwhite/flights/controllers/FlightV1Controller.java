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
import pw.peterwhite.flights.services.FlightService;

import java.time.LocalDateTime;
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

    /**
     * <b>Interconnections</b>: This API will return a list of available flights between the given airports and between the
     * given date-times. It will only return flight plans that have 0-1 stops and in the case of a interconnecting stop,
     * the departure of the second leg must be 2 hours after the arrival of the first leg.
     *
     * @param departure - IATA code for departure airport
     * @param arrival - IATA code for arrival airport
     * @param departureDateTime - Departure date-time
     * @param arrivalDateTime - Arrival date-time
     * @return List of available flights with details of stops and each leg
     */
    @RequestMapping(path = "/interconnections",
            method = GET,
            params = {"departure", "arrival", "departureDateTime", "arrivalDateTime"})
    public List<Flight> interconnections(String departure,
                                         String arrival,
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDateTime,
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateTime) {
        validateParams(departure, arrival, departureDateTime, arrivalDateTime);
        logger.debug("Valid params given");
        return flightService.getAvailableFlights(departure, arrival, departureDateTime, arrivalDateTime);
    }

    private void validateParams(String departure,
                                String arrival,
                                LocalDateTime departureDateTime,
                                LocalDateTime arrivalDateTime) {
        // Non-null departure/arrival Strings and also matching regex for IATA format (3-letter code)
        if (departure == null || !departure.toUpperCase().matches("^[A-Z]{3}$")
                || arrival == null || !arrival.toUpperCase().matches("^[A-Z]{3}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid departure or arrival codes");
        }

        if (departure.toUpperCase().equals(arrival.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure and arrival codes are the same");
        }

        if (arrivalDateTime.isBefore(departureDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arrival time is before departure time");
        }
    }
}
