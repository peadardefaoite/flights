package pw.peterwhite.flights.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.client.RestTemplate;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.config.ServiceTestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;
import pw.peterwhite.flights.helpers.TestHelper;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Service tests for
 */
@WebMvcTest(controllers = FlightV1Controller.class)
@Import(ServiceTestConfig.class)
class FlightV1ControllerFlightServiceTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RyanairApiClient ryanairApiClient;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    private void setup() {
        reset(ryanairApiClient);
        reset(restTemplate);
    }

    @AfterEach
    private void teardown() {
        reset(ryanairApiClient);
        reset(restTemplate);
    }

    @Test
    void ValidParams_Interconnections_isOk() throws Exception {
        //Arrange
        when(ryanairApiClient.getRoutes()).thenReturn(Collections.emptyList()); // return empty routes list

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_ARRIVAL)
                .param("departureDateTime", TestHelper.TEST_DEPARTURE_DATE_TIME_STRING)
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isOk()).andExpect(content().string("[]"));
        verify(ryanairApiClient, times(1)).getRoutes();

    }
}
