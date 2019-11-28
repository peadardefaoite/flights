package pw.peterwhite.flights;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import pw.peterwhite.flights.config.FlightConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;
import pw.peterwhite.flights.services.FlightService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FlightV1Controller.class)
class FlightV1ControllerTests {

    @Autowired
    private FlightConfig flightConfig;

    @Autowired
    private FlightService flightService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void test_givenNothing_getsHello_isOk() throws Exception{
        //given


        //when
        mockMvc.perform(get("/api/v1/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));

        //then
    }

    @Test
    void test_givenNothing_getsBlah_isNotFound() throws Exception{
        //given


        //when
        mockMvc.perform(get("/api/v1/Blah"))
                .andExpect(status().isOk());

        //then
    }

}
