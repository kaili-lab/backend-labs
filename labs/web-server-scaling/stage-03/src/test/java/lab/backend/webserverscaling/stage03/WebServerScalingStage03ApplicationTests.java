package lab.backend.webserverscaling.stage03;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * stage-03 接口测试。
 *
 * 测试重点是数据库接入后接口结构是否正确，
 * 以及重复读取实验是否可用。
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebServerScalingStage03ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 验证数据库版商品详情接口可用。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void databaseDetailsEndpointShouldReturnDatabaseStrategy() throws Exception {
        mockMvc.perform(get("/products/1/details/database"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.strategy").value("database"))
            .andExpect(jsonPath("$.parts.productInfo.value").value(org.hamcrest.Matchers.containsString("数据库版商品详情")));
    }

    /**
     * 验证数据库重复读取实验接口可用。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void repeatedReadEndpointShouldReturnDurations() throws Exception {
        mockMvc.perform(get("/experiments/database/repeated-read")
                .param("productId", "1")
                .param("times", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.times").value(3))
            .andExpect(jsonPath("$.durationsMs.length()").value(3))
            .andExpect(jsonPath("$.totalDurationMs").value(greaterThan(0)));
    }
}
