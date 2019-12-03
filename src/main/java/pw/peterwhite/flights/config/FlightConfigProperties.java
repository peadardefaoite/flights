package pw.peterwhite.flights.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

@ConfigurationProperties(prefix="flight")
public class FlightConfigProperties {
    @Autowired
    private Environment env;

    private String ryanairApiClientBaseUrl;
    private String routeOperator;

    public String getRyanairApiClientBaseUrl() {
        if (ryanairApiClientBaseUrl == null) {
            // No ryanairApiClientBaseUrl defined in properties, reading from environment and caching, as environment reads are expensive
            ryanairApiClientBaseUrl = env.getProperty("ryanairApiClientBaseUrl", "https://services-api.ryanair.com");
        }
        return ryanairApiClientBaseUrl;
    }

    public void setRyanairApiClientBaseUrl(String ryanairApiClientBaseUrl) {
        this.ryanairApiClientBaseUrl = ryanairApiClientBaseUrl;
    }

    public String getRouteOperator() {
        if (routeOperator == null) {
            // No routeOperator defined in properties, reading from environment and caching, as environment reads are expensive
            routeOperator = env.getProperty("routeOperator", "RYANAIR");
        }
        return routeOperator;
    }

    public void setRouteOperator(String routeOperator) {
        this.routeOperator = routeOperator;
    }
}
