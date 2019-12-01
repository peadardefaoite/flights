package pw.peterwhite.flights.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import pw.peterwhite.flights.config.ServiceTestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Basic service level test for Spring functionality. Hits non-existent endpoint, returns 404.
 * No network requests are made in this test.
 */
@WebMvcTest(controllers = FlightV1Controller.class)
@Import(ServiceTestConfig.class)
class FlightV1ControllerBasicTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void Test_NonExistentAPI_isNotFound() throws Exception {
        //Arrange

        //Act
        ResultActions resultsActions = mockMvc.perform(get("/api/v1/NonExistentAPI"))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultsActions.andExpect(status().isNotFound());
    }
}
