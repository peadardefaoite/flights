package pw.peterwhite.flights.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.services.FlightService;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@TestConfiguration
public class TestConfig {

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
        RyanairApiClient mockRyanairApiClient = mock(RyanairApiClient.class);
        when(mockRyanairApiClient.getSchedules()).thenReturn(new ArrayList<>());
        when(mockRyanairApiClient.getRoutes()).thenReturn(new ArrayList<>());
        return mockRyanairApiClient;

    }
}
