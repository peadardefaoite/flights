package pw.peterwhite.flights.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.client.RestTemplate;
import pw.peterwhite.flights.clients.RyanairApiClient;
import pw.peterwhite.flights.config.ClientTestConfig;
import pw.peterwhite.flights.config.ServiceTestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;
import pw.peterwhite.flights.helpers.LocalDateTimeAdapter;
import pw.peterwhite.flights.helpers.TestHelper;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Service tests for testing the RyanairApiClient level of code
 * Network requests are mocked out for RestTemplate(...)
 */
@WebMvcTest(controllers = FlightV1Controller.class)
@Import(ClientTestConfig.class)
class FlightV1ControllerRyanairApiClientTests {
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RyanairApiClient ryanairApiClient;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    private void setup() {
        reset(restTemplate);
    }

    @Test
    void ValidParams_Interconnections_isOk() throws Exception {
        //Arrange
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(new ParameterizedTypeReference<List<Route>>(){})))
                .thenReturn(null);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(Schedule.class)))
                .thenReturn(null);

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_ARRIVAL)
                .param("departureDateTime", TestHelper.TEST_DEPARTURE_DATE_TIME_STRING)
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isOk()).andExpect(content().string("[]"));

    }
}
