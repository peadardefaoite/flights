package pw.peterwhite.flights.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.services.FlightService;

import static org.mockito.Mockito.*;

@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    public FlightConfigProperties flightConfigProperties() {
        return spy(FlightConfigProperties.class);
    }

    @Bean
    public FlightService flightService() {
        return spy(FlightService.class);
    }

    @Bean
    public RyanairApiClient ryanairApiClient() {
        return spy(new RyanairApiClient(flightConfigProperties()));
    }
}
