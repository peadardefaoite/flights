package pw.peterwhite.flights.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.services.FlightService;

import static org.mockito.Mockito.*;

@TestConfiguration
public class ControllerTestConfig {

    @Bean
    public FlightConfigProperties flightConfigProperties() {
        return mock(FlightConfigProperties.class);
    }

    @Bean
    public FlightService flightService() {
        return mock(FlightService.class);
    }

    @Bean
    public RyanairApiClient ryanairApiClient() {
        return mock(RyanairApiClient.class);
    }

    @Bean
    public RestTemplate restTemplate() {
        return mock(RestTemplate.class);
    }

}
