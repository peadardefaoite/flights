package pw.peterwhite.flights.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.peterwhite.flights.services.FlightService;

@RestController
@RequestMapping("/api/v1")
public class FlightV1Controller {

    private static final Log logger = LogFactory.getLog(FlightV1Controller.class);
    private final FlightService flightService;

    @Autowired
    public FlightV1Controller(FlightService flightService) {
        this.flightService = flightService;
    }
    @RequestMapping("/hello")
    public String index() {
        logger.info("Received request");
        logger.info("Flight info: " + flightService.getFlightInfo());
        return "Greetings from Spring Boot!";
    }
}
