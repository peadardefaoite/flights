package pw.peterwhite.flights.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@RestController
@RequestMapping("/api/v1")
public class FlightV1Controller {

    private static final Log logger = LogFactory.getLog(FlightV1Controller.class);

    @RequestMapping("/hello")
    public String index() {
        logger.info("Received request");
        return "Greetings from Spring Boot!";
    }
}
