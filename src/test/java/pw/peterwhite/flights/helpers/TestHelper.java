package pw.peterwhite.flights.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * TestHelper class for common methods used across various tests
 */
public class TestHelper {
    public static final String TEST_DEPARTURE = "DUB";
    public static final String TEST_ARRIVAL = "SXF";
    // These date-times are in the far future. This will cause test breakages once this date has passed as parameter
    // validation in the controller checks if the DepartureDateTime is in the past.
    public static final String TEST_DEPARTURE_DATE_TIME_STRING = "2030-01-15T12:00";
    public static final String TEST_ARRIVAL_DATE_TIME_STRING = "2030-02-14T12:00";
    public static final LocalDateTime TEST_DEPARTURE_DATE_TIME = LocalDateTime.parse(TEST_DEPARTURE_DATE_TIME_STRING, DateTimeFormatter.ISO_DATE_TIME);
    public static final LocalDateTime TEST_ARRIVAL_DATE_TIME = LocalDateTime.parse(TEST_ARRIVAL_DATE_TIME_STRING, DateTimeFormatter.ISO_DATE_TIME);
    public static final String TEST_SCHEDULES_API = "https://my-test-domain.lol/timtbl/3/schedules/%s/%s/years/%s/months/%s";

    public static ResponseEntity<List<Route>> generateEmptyRoutesHttpResponse() {
        return ResponseEntity.ok().body(Collections.emptyList());
    }

    public static ResponseEntity<List<Route>> generateRoutesHttpResponse() {
        return ResponseEntity.ok().body(generateRouteList());
    }

    public static ResponseEntity<Schedule> generateBlankScheduleHttpResponse() {
        return ResponseEntity.ok().body(new Schedule(1, new ArrayList<>()));
    }

    public static ResponseEntity<Schedule> generateScheduleHttpResponse(String airportFrom, String airportTo, int year, int month) {
        File file;
        String filename = String.format("schedule-%s-%s-%s-%s.json", airportFrom, airportTo, year, month);
        Schedule schedule = new Schedule(month, Collections.emptyList());
        try {
            file = ResourceUtils.getFile("classpath:" + filename);
            ObjectMapper mapper = new ObjectMapper();
            schedule = mapper.readValue(file, Schedule.class);
        } catch (FileNotFoundException ex) {
            fail("json not found for tests: " + filename);
        } catch (IOException ex) {
            fail("Reading json file failed");
        }
        return ResponseEntity.ok().body(schedule);
    }

    private static List<Route> generateRouteList() {
        File file;
        List<Route> routes = new ArrayList<>();
        try {
            file = ResourceUtils.getFile("classpath:routes-subset.json");
            ObjectMapper mapper = new ObjectMapper();
            routes = mapper.readValue(file, new TypeReference<List<Route>>(){});
        } catch (FileNotFoundException ex) {
            fail("routes-subset.json not found for tests");
        } catch (IOException ex) {
            fail("Reading json file failed");
        }
        return routes;
    }
}
