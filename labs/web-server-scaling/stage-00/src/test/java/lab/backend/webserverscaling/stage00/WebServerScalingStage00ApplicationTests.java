package lab.backend.webserverscaling.stage00;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebServerScalingStage00ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingShouldReturnPong() throws Exception {
        mockMvc.perform(get("/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("pong"));
    }

    @Test
    void healthzShouldReturnUp() throws Exception {
        mockMvc.perform(get("/healthz"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}
