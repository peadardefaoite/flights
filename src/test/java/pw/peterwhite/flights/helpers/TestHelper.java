package pw.peterwhite.flights.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;
import pw.peterwhite.flights.dto.Journey;
import pw.peterwhite.flights.dto.Journey.Leg;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * TestHelper class for common methods used across various tests
 */
public class TestHelper {
    public static final String TEST_DEPARTURE = "DUB";
    public static final String TEST_ARRIVAL = "SXF";
    // These date-times are in the far future. This will cause test breakages once this date has passed as parameter
    // validation in the controller checks if the DepartureDateTime is in the past.
    public static final String TEST_DEPARTURE_DATE_TIME_STRING = "2030-01-01T12:00";
    public static final String TEST_ARRIVAL_DATE_TIME_STRING = "2030-01-02T12:00";
    public static final LocalDateTime TEST_DEPARTURE_DATE_TIME = LocalDateTime.parse(TEST_DEPARTURE_DATE_TIME_STRING, DateTimeFormatter.ISO_DATE_TIME);
    public static final LocalDateTime TEST_ARRIVAL_DATE_TIME = LocalDateTime.parse(TEST_ARRIVAL_DATE_TIME_STRING, DateTimeFormatter.ISO_DATE_TIME);

    private static List<Route> routes;

    public static List<Journey> generateFlightList(Map<String, String> parameters) {
        Leg leg = new Leg(parameters.get("departure"), parameters.get("arrival"),
                LocalDateTime.parse(parameters.get("departureDateTime"), DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse(parameters.get("arrivalDateTime"), DateTimeFormatter.ISO_DATE_TIME));
        List<Leg> legs = new ArrayList<>();
        legs.add(leg);

        Journey journey = new Journey(legs.size() - 1 , legs);

        List<Journey> journeyList = new ArrayList<>();
        journeyList.add(journey);
        return journeyList;
    }

    public static List<Route> generateRouteList() {
        if (routes == null) {
            File file;
            try {
                file = ResourceUtils.getFile("classpath:routes.json");
                ObjectMapper mapper = new ObjectMapper();
                routes = mapper.readValue(file, new TypeReference<List<Route>>(){});
            } catch (FileNotFoundException ex) {
                fail("routes.json not found for tests");
            } catch (IOException ex) {
                fail("Reading json file failed");
            }
        }
        return routes;
    }

    public static List<Leg> generateLegsList() {
        return new ArrayList<>();
    }
}
