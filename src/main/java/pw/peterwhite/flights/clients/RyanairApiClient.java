package pw.peterwhite.flights.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import pw.peterwhite.flights.config.FlightConfigProperties;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RyanairApiClient extends Client {
    private static final Log logger = LogFactory.getLog(RyanairApiClient.class);

    @Autowired
    public RyanairApiClient(FlightConfigProperties flightConfigProperties) {
        this.baseUrl = flightConfigProperties.getRyanairApiClientBaseUrl();
    }

    public List<Route> getRoutes() {
        RestTemplate restTemplate = new RestTemplate();

        final URI routesUri = URI.create(baseUrl + "/locate/3/routes/");
        logger.info("Making request to Routes API: " + routesUri);

        // Make call of Routes API
        ResponseEntity<List<Route>> result = restTemplate.exchange(routesUri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Route>>() {
        });

        if (result.getStatusCode() != HttpStatus.OK) {
            logger.info("Routes API down");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Downstream Routes API is not working");
        }
        return new ArrayList<>();
    }

    public List<Schedule> getSchedules() {
        //TODO: Make call to Schedules API
        return new ArrayList<>();
    }
}