package pw.peterwhite.flights;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pw.peterwhite.flights.config.LocalDateTimeAdapter;
import pw.peterwhite.flights.config.TestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;
import pw.peterwhite.flights.dto.Flight;
import pw.peterwhite.flights.dto.Leg;
import pw.peterwhite.flights.services.FlightService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlightV1Controller.class)
@Import(TestConfig.class)
class FlightV1ControllerTests {
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;

    @BeforeEach
    private void setup() {
        reset(flightService);
    }

    @Test
    void test_givenNothing_getsHello_isOk() throws Exception {
        //Arrange
        when(flightService.getFlightInfo()).thenReturn(3);

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/hello"));

        //Assert
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));
        verify(flightService, times(1)).getFlightInfo();
    }

    @Test
    void Test_NonExistentAPI_isNotFound() throws Exception {
        //Arrange

        //Act
        ResultActions resultsActions = mockMvc.perform(get("/api/v1/NonExistentAPI"));

        //Assert
        resultsActions.andExpect(status().isNotFound());
        verify(flightService, never()).getFlightInfo();
    }

    @Test
    void Test_InterconnectionsNoParams_isBadRequest() throws Exception {
        //Arrange

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections"));

        //Assert
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void ValidParams_Interconnections_isOk() throws Exception {
        //Arrange
        String departure = "DUB";
        String arrival = "LHR";
        String departureDateTimeString = "2012-12-12T12:00";
        String arrivalDateTimeString = "2012-12-12T14:00";

        LocalDateTime departureDateTime = LocalDateTime.parse(departureDateTimeString, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDateTimeString, DateTimeFormatter.ISO_DATE_TIME);

        List<Flight> flightList = new ArrayList<>();

        Leg leg = new Leg(departure, arrival, departureDateTime, arrivalDateTime);
        List<Leg> legs = new ArrayList<>();
        legs.add(leg);

        flightList.add(new Flight(0, legs));

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", departure)
                .param("arrival", arrival)
                .param("departureDateTime", departureDateTimeString)
                .param("arrivalDateTime", arrivalDateTimeString)
        );

        //Assert
        resultActions.andExpect(status().isOk()).andExpect(content().string(gson.toJson(flightList)));
    }

    @Test
    void InvalidDateTime_Interconnections_isBadRequest() throws Exception {
        //Arrange
        String departure = "DUB";
        String arrival = "LHR";
        String departureDateTimeString = "2012-13-13T25:00";
        String arrivalDateTimeString = "2012-12-12T14:00";

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", departure)
                .param("arrival", arrival)
                .param("departureDateTime", departureDateTimeString)
                .param("arrivalDateTime", arrivalDateTimeString)
        );

        //Assert
        resultActions.andExpect(status().isBadRequest());
    }


    @Test
    void ArrivalBeforeDeparture_Interconnections_isBadRequest() throws Exception {
        //Arrange
        String departure = "DUB";
        String arrival = "LHR";
        String departureDateTimeString = "2012-12-12T12:00";
        String arrivalDateTimeString = "2012-11-11T14:00";

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", departure)
                .param("arrival", arrival)
                .param("departureDateTime", departureDateTimeString)
                .param("arrivalDateTime", arrivalDateTimeString)
        );

        //Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(status().reason("Arrival time is before departure time"));
    }

    @Test
    void InvalidAirportParams_Interconnections_isBadRequest() throws Exception {
        //Arrange
        String departure = "123";
        String arrival = "456";
        String departureDateTimeString = "2012-12-12T12:00";
        String arrivalDateTimeString = "2012-12-12T14:00";

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", departure)
                .param("arrival", arrival)
                .param("departureDateTime", departureDateTimeString)
                .param("arrivalDateTime", arrivalDateTimeString)
        );

        //Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(status().reason("Invalid departure or arrival codes"));
    }


    @Test
    void SameAirportParams_Interconnections_isBadRequest() throws Exception {
        //Arrange
        String departure = "DUB";
        String arrival = "DUB";
        String departureDateTimeString = "2012-12-12T12:00";
        String arrivalDateTimeString = "2012-12-12T14:00";

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", departure)
                .param("arrival", arrival)
                .param("departureDateTime", departureDateTimeString)
                .param("arrivalDateTime", arrivalDateTimeString)
        );

        //Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(status().reason("Departure and arrival codes are the same"));
    }
}
