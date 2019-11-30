package pw.peterwhite.flights.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.config.IntegrationTestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;
import pw.peterwhite.flights.dto.Flight;
import pw.peterwhite.flights.helpers.LocalDateTimeAdapter;
import pw.peterwhite.flights.helpers.TestHelper;
import pw.peterwhite.flights.services.FlightService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlightV1Controller.class)
@Import(IntegrationTestConfig.class)
class FlightV1ControllerRoutesApiIntegrationTest {
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;

    @Autowired
    private RyanairApiClient ryanairApiClient;

    @BeforeEach
    private void setup() {
        reset(flightService);
        reset(ryanairApiClient);
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

        List<Flight> flightList = TestHelper.generateFlightList(departure, arrival, departureDateTime, arrivalDateTime);

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", departure)
                .param("arrival", arrival)
                .param("departureDateTime", departureDateTimeString)
                .param("arrivalDateTime", arrivalDateTimeString))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isOk()).andExpect(content().string(gson.toJson(flightList)));
        verify(flightService, times(1))
                .getAvailableFlights(departure, arrival, departureDateTime, arrivalDateTime);
        verify(ryanairApiClient, times(1)).getRoutes();
        verify(ryanairApiClient, times(1)).getSchedules();
    }
}
