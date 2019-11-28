package pw.peterwhite.flights.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pw.peterwhite.flights.services.FlightService;

@TestConfiguration
public class TestConfig {

    @Bean
    public FlightService flightService(){
        FlightService mockFlightService = mock(FlightService.class);
        when(mockFlightService.getFlightInfo()).thenReturn(1);
        return mockFlightService;
    }
}
