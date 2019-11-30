package pw.peterwhite.flights.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.services.FlightService;

@Configuration
@ComponentScan(basePackages = "pw.peterwhite.flights")
public class FlightConfig {
    private static final Log logger = LogFactory.getLog(FlightConfig.class);

    @Bean
    public FlightConfigProperties flightConfigProperties() {
        return new FlightConfigProperties();
    }

    @Bean
    public FlightService flightService(){
        return new FlightService();
    }

    @Bean
    public RyanairApiClient ryanairApiClient() {
        return new RyanairApiClient();
    }
}