package pw.peterwhite.flights.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.client.RestTemplate;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.services.FlightService;

@Configuration
@ComponentScan(basePackages = "pw.peterwhite.flights")
public class FlightConfig {
    @Bean
    public FlightConfigProperties flightConfigProperties() {
        return new FlightConfigProperties();
    }

    @Bean
    public FlightService flightService(){
        return new FlightService();
    }

    @Bean
    @DependsOn("flightConfigProperties")
    public RyanairApiClient ryanairApiClient() {
        return new RyanairApiClient(flightConfigProperties());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}