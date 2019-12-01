package pw.peterwhite.flights.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
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
    private RestTemplate restTemplate;

    @Autowired
    public RyanairApiClient(FlightConfigProperties flightConfigProperties) {
        this.baseUrl = flightConfigProperties.getRyanairApiClientBaseUrl();
    }

    public List<Route> getRoutes() {
        final URI routesUri = URI.create(baseUrl + "/locate/3/routes/");
        logger.info("Making request to Routes API: " + routesUri);
        ResponseEntity<List<Route>> result;
        try {
            // Make call of Routes API
             result = restTemplate.exchange(routesUri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Route>>() {
                    });

        } catch (RestClientException ex) {
            logger.error("Routes API Client error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "An error occurred when communicating with the Routes API", ex);
        }

        handle(result);
        if (result.getStatusCode() != HttpStatus.OK) {
            logger.info("Routes API down");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Downstream Routes API is not working");
        }

        return result.getBody();
    }

    public List<Schedule> getSchedules() {
        //TODO: Make call to Schedules API
        return new ArrayList<>();
    }

    private void handle(ResponseEntity result) throws ResponseStatusException {
        if (result.getStatusCode().is2xxSuccessful()) {

        }

        if (result.getStatusCode().is5xxServerError()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error in returning");
        }
    }
}