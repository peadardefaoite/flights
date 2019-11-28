package pw.peterwhite.flights.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import pw.peterwhite.flights.services.FlightService;

@Configuration
@ComponentScan(basePackages = "pw.peterwhite.flights")
public class FlightConfig {
    private static final Log logger = LogFactory.getLog(FlightConfig.class);

    @Bean
    public FlightService flightService(){
        logger.info("Creating new flight service in config");
        return new FlightService();
    }

}