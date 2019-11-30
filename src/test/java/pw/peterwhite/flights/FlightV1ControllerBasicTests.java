package pw.peterwhite.flights;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import pw.peterwhite.flights.config.TestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;

@WebMvcTest(controllers = FlightV1Controller.class)
@Import(TestConfig.class)
class FlightV1ControllerBasicTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void Test_NonExistentAPI_isNotFound() throws Exception {
        //Arrange

        //Act
        ResultActions resultsActions = mockMvc.perform(get("/api/v1/NonExistentAPI"));

        //Assert
        resultsActions.andExpect(status().isNotFound());
    }

}
