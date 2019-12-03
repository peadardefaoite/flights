package pw.peterwhite.flights.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pw.peterwhite.flights.dto.Journey;
import pw.peterwhite.flights.services.FlightService;

import java.time.LocalDate;
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
     * @param departureDateTime - Departure date-time in departure airport timezone
     * @param arrivalDateTime - Arrival date-time in arrival airport timezone
     * @return List of available flights with details of stops and each leg
     */
    @RequestMapping(path = "/interconnections",
            method = GET,
            params = {"departure", "arrival", "departureDateTime", "arrivalDateTime"})
    public List<Journey> interconnections(String departure,
                                          String arrival,
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDateTime,
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateTime) {
        validateParams(departure, arrival, departureDateTime, arrivalDateTime);

        return flightService.getAvailableFlights(departure.toUpperCase(), arrival.toUpperCase(), departureDateTime, arrivalDateTime);
    }

    private void validateParams(String departure,
                                String arrival,
                                LocalDateTime departureDateTime,
                                LocalDateTime arrivalDateTime) {
        if (departureDateTime == null || arrivalDateTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure/arrival date-times must be provided");
        }

        if (departure == null || arrival == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid departure or arrival codes");
        }


        LocalDate today = LocalDate.now();
        if (departureDateTime.toLocalDate().isBefore(today)) {
            // Use date instead of date-time as if the user submits the current minute, it may be a valid request
            // but by the time this code is reached, LocalDateTime.now() might be in the next minute. This will happen
            // much less with LocalDate and only potentially near when midnight occurs.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure date is in the past");
        }

        // Non-null departure/arrival Strings and also matching regex for IATA format (3-letter code)
        if (!departure.toUpperCase().matches("^[A-Z]{3}$")
                || !arrival.toUpperCase().matches("^[A-Z]{3}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid departure or arrival codes");
        }

        if (departure.toUpperCase().equals(arrival.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure and arrival codes are the same");
        }

        // There was previously a condition here that arrivalDateTime.isAfter(departureDateTime), but as they are
        // *local* time-zones for each airport, it is possible that local time at arrival can be before the local time
        // you departed at. Eg if a flight takes 30 minutes but you fly into a timezone 1 hour earlier than the departure
        // airport's timezone.

        logger.debug("Valid params given");
    }
}
