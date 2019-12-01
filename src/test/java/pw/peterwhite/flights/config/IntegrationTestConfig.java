package pw.peterwhite.flights.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.services.FlightService;

import static org.mockito.Mockito.spy;

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

    @Bean
    public RestTemplate restTemplate() {
        return spy(RestTemplate.class);
    }
}
