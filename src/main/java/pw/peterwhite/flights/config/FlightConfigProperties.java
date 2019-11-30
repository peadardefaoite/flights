package pw.peterwhite.flights.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

import java.net.URI;

@ConfigurationProperties(prefix="flight")
public class FlightConfigProperties {
    private static final Log logger = LogFactory.getLog(FlightConfigProperties.class);

    @Autowired
    private Environment env;

    private String ryanairApiClientBaseUrl;

    public String getRyanairApiClientBaseUrl() {
        if (ryanairApiClientBaseUrl == null) {
            ryanairApiClientBaseUrl = env.getProperty("ryanairApiClientBaseUrl", "https://services-api.ryanair.com");
        }
        return ryanairApiClientBaseUrl;
    }

    public void setRyanairApiClientBaseUrl(String ryanairApiClientBaseUrl) {
        this.ryanairApiClientBaseUrl = ryanairApiClientBaseUrl;
    }

}
