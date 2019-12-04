package pw.peterwhite.flights.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.services.FlightService;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestConfiguration
public class ServiceTestConfig {

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
        RyanairApiClient mockRyanairApiClient = mock(RyanairApiClient.class);
        when(mockRyanairApiClient.getSchedules(any(), any(), any())).thenReturn(new ArrayList<>());
        when(mockRyanairApiClient.getRoutes()).thenReturn(new ArrayList<>());
        return mockRyanairApiClient;
    }

    @Bean
    public RestTemplate restTemplate() {
        return mock(RestTemplate.class);
    }
}
