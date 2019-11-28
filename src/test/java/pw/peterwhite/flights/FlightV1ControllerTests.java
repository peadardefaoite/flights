package pw.peterwhite.flights;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import pw.peterwhite.flights.config.TestConfig;
import pw.peterwhite.flights.controllers.FlightV1Controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FlightV1Controller.class)
@Import(TestConfig.class)
class FlightV1ControllerTests {

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
                .andExpect(status().is4xxClientError());

        //then
    }

}
