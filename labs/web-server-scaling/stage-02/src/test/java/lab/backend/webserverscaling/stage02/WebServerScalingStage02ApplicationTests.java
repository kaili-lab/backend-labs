package lab.backend.webserverscaling.stage02;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * stage-02 接口测试。
 *
 * 测试重点是接口结构、策略名称和线程池受控特征是否可见，
 * 而不是精确压测耗时。
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebServerScalingStage02ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 验证商品详情线程池聚合接口可用。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void productDetailsEndpointShouldReturnThreadPoolStrategy() throws Exception {
        mockMvc.perform(get("/products/1/details/thread-pool"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.strategy").value("thread-pool"))
            .andExpect(jsonPath("$.parts.productInfo.threadName").value(org.hamcrest.Matchers.startsWith("product-aggregation-")));
    }

    /**
     * 验证批量任务实验接口可用，
     * 并且受控线程池不会为每个任务都创建一个全新线程。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void batchExperimentShouldReuseBoundedPoolThreads() throws Exception {
        mockMvc.perform(get("/experiments/thread-pool/batch")
                .param("taskCount", "6")
                .param("delayMs", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.strategy").value("thread-pool-batch"))
            .andExpect(jsonPath("$.taskCount").value(6))
            .andExpect(jsonPath("$.tasks.length()").value(6))
            .andExpect(jsonPath("$.tasks[0].threadName").value(org.hamcrest.Matchers.startsWith("product-aggregation-")))
            .andExpect(jsonPath("$.totalDurationMs").value(lessThanOrEqualTo(500)));
    }
}
