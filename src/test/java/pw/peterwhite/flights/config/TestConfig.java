package pw.peterwhite.flights.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pw.peterwhite.flights.services.FlightService;

import static org.mockito.Mockito.*;

@TestConfiguration
public class TestConfig {

    @Bean
    public FlightService flightService(){
        return spy(FlightService.class);
    }
}
