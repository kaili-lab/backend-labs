package lab.backend.webserverscaling.stage01;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * stage-01 接口测试。
 *
 * 这里的测试重点不是压测耗时，
 * 而是先保证串行版和并发版的接口结构都正确可用。
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebServerScalingStage01ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 验证串行接口返回的策略标识和基本结构正确。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void serialEndpointShouldReturnSerialStrategy() throws Exception {
        mockMvc.perform(get("/products/1/details/serial"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.strategy").value("serial"))
            .andExpect(jsonPath("$.parts.productInfo.name").value("productInfo"));
    }

    /**
     * 验证并发接口返回的策略标识和基本结构正确。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void concurrentEndpointShouldReturnConcurrentStrategy() throws Exception {
        mockMvc.perform(get("/products/1/details/concurrent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.strategy").value("concurrent"))
            .andExpect(jsonPath("$.parts.recommendations.name").value("recommendations"));
    }
}
