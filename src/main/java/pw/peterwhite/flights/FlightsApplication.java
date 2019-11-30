package pw.peterwhite.flights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pw.peterwhite.flights.config.FlightConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(FlightConfigProperties.class)
public class FlightsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightsApplication.class, args);
    }

}
