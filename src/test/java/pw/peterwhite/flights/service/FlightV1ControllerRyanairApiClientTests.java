package pw.peterwhite.flights.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pw.peterwhite.flights.config.ClientTestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;
import pw.peterwhite.flights.dto.Journey;
import pw.peterwhite.flights.dto.Route;
import pw.peterwhite.flights.dto.Schedule;
import pw.peterwhite.flights.helpers.TestHelper;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pw.peterwhite.flights.helpers.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testing the RyanairApiClient level of code
 * Network requests are mocked out for RestTemplate(...)
 */
@WebMvcTest(controllers = FlightV1Controller.class)
@Import(ClientTestConfig.class)
class FlightV1ControllerRyanairApiClientTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    private void setup() {
        reset(restTemplate);
    }

    @Test
    void givenNoRoutes_Interconnections_returnsEmptyList() throws Exception {
        //Arrange
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(new ParameterizedTypeReference<List<Route>>(){})))
                .thenReturn(TestHelper.generateEmptyRoutesHttpResponse());

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_ARRIVAL)
                .param("departureDateTime", TestHelper.TEST_DEPARTURE_DATE_TIME_STRING)
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isOk()).andExpect(content().string("[]"));
        verify(restTemplate, times(1)).exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(new ParameterizedTypeReference<List<Route>>(){}));
        verify(restTemplate, never()).exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(Schedule.class));
    }

    @Test
    void givenRoutesButNoSchedules_Interconnections_returnsEmptyList() throws Exception {
        //Arrange
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(new ParameterizedTypeReference<List<Route>>(){})))
                .thenReturn(TestHelper.generateRoutesHttpResponse());
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(Schedule.class))).thenReturn(TestHelper.generateBlankScheduleHttpResponse());

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_ARRIVAL)
                .param("departureDateTime", TestHelper.TEST_DEPARTURE_DATE_TIME_STRING)
                .param("arrivalDateTime", TestHelper.TEST_ARRIVAL_DATE_TIME_STRING))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultActions.andExpect(status().isOk()).andExpect(content().string("[]"));

        // Expect 1 call to Routes API
        verify(restTemplate, times(1)).exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(new ParameterizedTypeReference<List<Route>>(){}));

        // Expect 6 calls to: 1xDirect, 2x FirstLeg, 0x SecondLeg and each for 2 months
        verify(restTemplate, times(6)).exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(Schedule.class));
    }

    @Test
    void givenRoutesAndSchedulesTwoDays_Interconnections_returnsSevenJourneys() throws Exception {
        /* Given DUB - SXF between 2030-01-31T12:00 and 2030-02-01T23:00.

           Calculated from JSON Schedules in /src/test/resources.
           2x Direct Flights (at 2030-01-31T13:50 and 2030-02-01T14:20)
           Indirect
             - DUB -> ACE -> SXF (none)
                - 2x First Leg flights (at 2030-02-01T06:25 and 2030-02-01T13:55)
                - No flights found in time period and no schedule available for February for ACE-SXF (404)
             - DUB -> BCN -> SXF (5x suitable)
                - 4x First Leg flights
                    - 2030-01-31T(12:30 - 16:00) A
                    - 2030-01-31T(17:10 - 20:40) B
                    - 2030-02-01T(06:15 - 09:45) C
                    - 2030-02-01T(17:10 - 20:40) D
                - 3x Second Leg flights
                    - 2030-01-31T(16:25 - 19:10) E
                    - 2030-02-01T(06:35 - 09:20) F
                    - 2030-02-01T(16:25 - 19:10) G

           Calculating which flights are suitable for the BCN journey,
           5 meet the requirements with 2 hour stopover in BCN: AF, AG, BF, BG, CG

           Expected calls to Routes API: 1
           Expected calls to Schedules API: 10 (will need 10 URIs and 9 Schedules to be returned)
           Total suitable flights: 7
         */


        //Arrange
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(new ParameterizedTypeReference<List<Route>>(){})))
                .thenReturn(TestHelper.generateRoutesHttpResponse());


        int year = 2030; int firstMonth = 1; int secondMonth = 2;
        String aceAirport = "ACE"; String bcnAirport = "BCN";

        // Direct Flights. Schedules URIs and their JSON responses
        URI Uri_direct_Month1 = URI.create(String.format(TEST_SCHEDULES_API, TEST_DEPARTURE, TEST_ARRIVAL, year, firstMonth));
        URI Uri_direct_Month2 = URI.create(String.format(TEST_SCHEDULES_API, TEST_DEPARTURE, TEST_ARRIVAL, year, secondMonth));

        ResponseEntity<Schedule> Schedule_direct_Month1 = generateScheduleHttpResponse(TEST_DEPARTURE, TEST_ARRIVAL, year, firstMonth);
        ResponseEntity<Schedule> Schedule_direct_Month2 = generateScheduleHttpResponse(TEST_DEPARTURE, TEST_ARRIVAL, year, secondMonth);

        // DUB - ACE - SXF. Schedules URIs and their JSON responses
        URI Uri_aceLeg1_Month1 = URI.create(String.format(TEST_SCHEDULES_API, TEST_DEPARTURE, aceAirport, year, firstMonth));
        URI Uri_aceLeg1_Month2 = URI.create(String.format(TEST_SCHEDULES_API, TEST_DEPARTURE, aceAirport, year, secondMonth));
        URI Uri_aceLeg2_Month1 = URI.create(String.format(TEST_SCHEDULES_API, aceAirport, TEST_ARRIVAL, year, firstMonth));
        URI Uri_aceLeg2_Month2 = URI.create(String.format(TEST_SCHEDULES_API, aceAirport, TEST_ARRIVAL, year, secondMonth));

        ResponseEntity<Schedule> Schedule_aceLeg1_Month1 = generateScheduleHttpResponse(TEST_DEPARTURE, aceAirport, year, firstMonth);
        ResponseEntity<Schedule> Schedule_aceLeg1_Month2 = generateScheduleHttpResponse(TEST_DEPARTURE, aceAirport, year, secondMonth);
        ResponseEntity<Schedule> Schedule_aceLeg2_Month1 = generateScheduleHttpResponse(aceAirport, TEST_ARRIVAL, year, firstMonth);
        HttpClientErrorException Exception_aceLeg2_Month2 = new HttpClientErrorException(HttpStatus.NOT_FOUND);

        // DUB - BCN - SXF. Schedules URIs and their JSON responses
        URI Uri_bcnLeg1_Month1 = URI.create(String.format(TEST_SCHEDULES_API, TEST_DEPARTURE, bcnAirport, year, firstMonth));
        URI Uri_bcnLeg1_Month2 = URI.create(String.format(TEST_SCHEDULES_API, TEST_DEPARTURE, bcnAirport, year, secondMonth));
        URI Uri_bcnLeg2_Month1 = URI.create(String.format(TEST_SCHEDULES_API, bcnAirport, TEST_ARRIVAL, year, firstMonth));
        URI Uri_bcnLeg2_Month2 = URI.create(String.format(TEST_SCHEDULES_API, bcnAirport, TEST_ARRIVAL, year, secondMonth));

        ResponseEntity<Schedule> Schedule_bcnLeg1_Month1 = generateScheduleHttpResponse(TEST_DEPARTURE, bcnAirport, year, firstMonth);
        ResponseEntity<Schedule> Schedule_bcnLeg1_Month2 = generateScheduleHttpResponse(TEST_DEPARTURE, bcnAirport, year, secondMonth);
        ResponseEntity<Schedule> Schedule_bcnLeg2_Month1 = generateScheduleHttpResponse(bcnAirport, TEST_ARRIVAL, year, firstMonth);
        ResponseEntity<Schedule> Schedule_bcnLeg2_Month2 = generateScheduleHttpResponse(bcnAirport, TEST_ARRIVAL, year, secondMonth);

        // Mocking network calls to the above URIs and returning their expected JSON responses, read from the files
        when(restTemplate.exchange(eq(Uri_direct_Month1), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_direct_Month1);
        when(restTemplate.exchange(eq(Uri_direct_Month2), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_direct_Month2);
        when(restTemplate.exchange(eq(Uri_aceLeg1_Month1), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_aceLeg1_Month1);
        when(restTemplate.exchange(eq(Uri_aceLeg1_Month2), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_aceLeg1_Month2);
        when(restTemplate.exchange(eq(Uri_aceLeg2_Month1), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_aceLeg2_Month1);
        when(restTemplate.exchange(eq(Uri_aceLeg2_Month2), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenThrow(Exception_aceLeg2_Month2);
        when(restTemplate.exchange(eq(Uri_bcnLeg1_Month1), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_bcnLeg1_Month1);
        when(restTemplate.exchange(eq(Uri_bcnLeg1_Month2), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_bcnLeg1_Month2);
        when(restTemplate.exchange(eq(Uri_bcnLeg2_Month1), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_bcnLeg2_Month1);
        when(restTemplate.exchange(eq(Uri_bcnLeg2_Month2), eq(HttpMethod.GET), eq(null), eq(Schedule.class))).thenReturn(Schedule_bcnLeg2_Month2);

        //Act
        ResultActions resultActions = mockMvc.perform(get("/api/v1/interconnections")
                .param("departure", TestHelper.TEST_DEPARTURE)
                .param("arrival", TestHelper.TEST_ARRIVAL)
                .param("departureDateTime", LocalDateTime.of(year,firstMonth,31,12,0).toString())
                .param("arrivalDateTime", LocalDateTime.of(year,secondMonth,1,23,0).toString()))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        MvcResult result = resultActions.andExpect(status().isOk()).andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        List<Journey> actualJourney = objectMapper.readValue(jsonResponse, new TypeReference<List<Journey>>() {});

        assertEquals(7, actualJourney.size()); // 7 Journeys
        assertEquals(2, actualJourney.stream().filter(journey -> journey.getStops() == 0).count()); // 2 Direct
        assertEquals(5, actualJourney.stream().filter(journey -> journey.getStops() == 1).count()); // 5 with 1 stop

        // Expect 1 call to Routes API
        verify(restTemplate, times(1)).exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(new ParameterizedTypeReference<List<Route>>(){}));

        // Expect 10 calls to: 1xDirect, 2x FirstLeg, 2x SecondLeg and each for 2 months
        verify(restTemplate, times(10)).exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
                eq(Schedule.class));
    }
}
