package pw.peterwhite.flights;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class FlightController {

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
}
