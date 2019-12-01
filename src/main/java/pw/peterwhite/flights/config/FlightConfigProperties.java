package pw.peterwhite.flights.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

@ConfigurationProperties(prefix="flight")
public class FlightConfigProperties {
    private static final Log logger = LogFactory.getLog(FlightConfigProperties.class);

    @Autowired
    private Environment env;

    private String ryanairApiClientBaseUrl;

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

}
