package pw.peterwhite.flights.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import pw.peterwhite.flights.config.ControllerTestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;
import pw.peterwhite.flights.helpers.TestHelper;
import pw.peterwhite.flights.services.FlightService;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Service tests for validation of request parameters in the FlightV1Controller class.
 * Calls to flightService are mocked out. Network requests are also not made in this suite.
 */
@WebMvcTest(controllers = FlightV1Controller.class)
@Import(ControllerTestConfig.class)
class FlightV1ControllerParamValidationServiceTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;

    @BeforeEach
    private void setup() {
        reset(flightService);
    }

    @Test
    void noParams_Interconnections_isBadRequest() throws Exception {
        //Arrange

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections"))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(status().reason("Parameter conditions \"departure, arrival, departureDateTime, " +
                        "arrivalDateTime\" not met for actual request parameters: "));
        verify(flightService, never()).getAvailableFlights(any(), any(), any(), any());
    }


    @Test
    void emptyParams_Interconnections_isBadRequest() throws Exception {
        //Arrange

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", "")
                .param("arrival", "")
                .param("departureDateTime", "")
                .param("arrivalDateTime", ""))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(status().reason("Departure/arrival date-times must be provided"));
        verify(flightService, never()).getAvailableFlights(any(), any(), any(), any());
    }

    @Test
    void invalidDateTime_Interconnections_isBadRequest() throws Exception {
        //Arrange
        String departureDateTimeString = "2012-13-13T25:00"; // Invalid departure, cannot be parsed by LocalDateTime

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_ARRIVAL)
                .param("departureDateTime", departureDateTimeString) // pass invalid param
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isBadRequest());
        verify(flightService, never()).getAvailableFlights(any(), any(), any(), any());
    }

    @Test
    void departureBeforeNow_Interconnections_isBadRequest() throws Exception {
        //Arrange
        String departureDateTimeString = "1999-12-31T23:59";

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_ARRIVAL)
                .param("departureDateTime", departureDateTimeString)
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(status().reason("Departure date is in the past"));
        verify(flightService, never()).getAvailableFlights(any(), any(), any(), any());
    }

    @Test
    void invalidAirportParams_Interconnections_isBadRequest() throws Exception {
        //Arrange
        String departure = "123"; // Does not match IATA codes
        String arrival = "456";

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", departure)
                .param("arrival", arrival)
                .param("departureDateTime", TestHelper.TEST_DEPARTURE_DATE_TIME_STRING)
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(status().reason("Invalid departure or arrival codes"));
        verify(flightService, never()).getAvailableFlights(any(), any(), any(), any());
    }

    @Test
    void sameAirportParams_Interconnections_isBadRequest() throws Exception {
        //Arrange

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_DEPARTURE)
                .param("departureDateTime", TestHelper.TEST_DEPARTURE_DATE_TIME_STRING)
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isBadRequest())
                .andExpect(status().reason("Departure and arrival codes are the same"));
        verify(flightService, never()).getAvailableFlights(any(), any(), any(), any());
    }

    @Test
    void validParams_Interconnections_isOk() throws Exception {
        //Arrange
        //Mock call to flightService as we're only testing Parameter Validation in this suite
        when(flightService.getAvailableFlights(anyString(), anyString(),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_ARRIVAL)
                .param("departureDateTime", TestHelper.TEST_DEPARTURE_DATE_TIME_STRING)
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isOk()).andExpect(content().string("[]"));
        // verify we pass validation and go to call flightService.getAvailableFlights(...)
        verify(flightService, times(1))
                .getAvailableFlights(TestHelper.TEST_DEPARTURE, TestHelper.TEST_ARRIVAL,
                        TestHelper.TEST_DEPARTURE_DATE_TIME, TestHelper.TEST_ARRIVAL_DATE_TIME);
    }
}
