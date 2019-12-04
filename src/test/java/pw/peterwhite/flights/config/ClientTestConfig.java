package pw.peterwhite.flights.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.services.FlightService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@TestConfiguration
public class ClientTestConfig {

    @Bean
    public FlightConfigProperties flightConfigProperties() {
        return spy(FlightConfigProperties.class);
    }

    @Bean
    public FlightService flightService() {
        return new FlightService();
    }

    @Bean
    public RyanairApiClient ryanairApiClient() {
        RyanairApiClient ryanairApiClient = new RyanairApiClient(flightConfigProperties());
        ryanairApiClient.setBaseUrl("https://my-test-domain.lol");
        return ryanairApiClient;
    }

    @Bean
    public RestTemplate restTemplate() {
        return mock(RestTemplate.class);
    }
}
