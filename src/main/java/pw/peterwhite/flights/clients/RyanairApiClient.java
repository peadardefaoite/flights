package pw.peterwhite.flights.clients;

import com.fasterxml.jackson.databind.JsonMappingException;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class RyanairApiClient extends Client {
    private static final Log logger = LogFactory.getLog(RyanairApiClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public RyanairApiClient(FlightConfigProperties flightConfigProperties) {
        this.baseUrl = flightConfigProperties.getRyanairApiClientBaseUrl();
    }

    /**
     * <b>getRoutes</b>:
     *
     * @return List of all routes from API
     */
    public List<Route> getRoutes() {
        final URI routesApi = URI.create(baseUrl + "/locate/3/routes/");
        logger.info("Making request to Routes API: " + routesApi);
        ResponseEntity<List<Route>> result;
        try {
            // Make call of Routes API using restTemplate.exchange(url, httpMethod, requestEntity, responseType)
             result = restTemplate.exchange(routesApi,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Route>>() {
                    });
        } catch (RestClientException ex) {
            logger.error("Routes API Client error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred when communicating with the Routes API");
        } catch (Exception ex) {
            logger.error("Routes API Response error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred with the response from the Routes API");
        }
        handleResponse(result);

        List<Route> routes = result.getBody();
        if (routes != null) {
            routes.removeIf(Objects::isNull);
        }

        return routes;
    }

    public List<Schedule> getSchedules(List<Route> listOfRoutes, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {


        for (Route route : listOfRoutes) {
            for (LocalDateTime dateTime = departureDateTime; dateTime.isBefore(arrivalDateTime); dateTime = dateTime.plusMonths(1)) {
                String resource = String.format("/timtbl/3/%s/%s/years/%s/month/%s", route.airportFrom, route.airportTo, dateTime.getYear(), dateTime.getMonthValue());
                URI schedulesApi = URI.create(baseUrl + resource);
                ResponseEntity<List<Schedule>> result;
                try {
                    // Make call Schedules API using restTemplate.exchange(url, httpMethod, requestEntity, responseType)
                    result = restTemplate.exchange(schedulesApi,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Schedule>>() {
                            });
                } catch (RestClientException ex) {
                    logger.error("Schedules API Client error: " + ex.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred when communicating with the Schedules API");
                } catch (Exception ex) {
                    logger.error("Routes API Response error: " + ex.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred with the response from the Routes API");
                }
                handleResponse(result);
            }
        }


        //TODO: Make call to Schedules API
        return new ArrayList<>();
    }

    private void handleResponse(ResponseEntity result) throws ResponseStatusException {
        if (result.getStatusCode().is4xxClientError()) {
            logger.error("Error in information supplied to Upstream API");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in information supplied to Upstream API");
        }

        if (result.getStatusCode().is5xxServerError()) {
            logger.warn("Error in Upstream API, potentially down");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error in Upstream API, potentially down");
        }
    }
}