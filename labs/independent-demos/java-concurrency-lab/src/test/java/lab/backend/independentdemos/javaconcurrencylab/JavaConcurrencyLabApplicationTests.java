package lab.backend.independentdemos.javaconcurrencylab;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 并发武器库实验接口测试。
 *
 * 这些测试主要保证各个实验入口都能正常返回，
 * 不把测试目标放在精确耗时上，避免测试不稳定。
 */
@SpringBootTest
@AutoConfigureMockMvc
class JavaConcurrencyLabApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 验证直接创建线程实验接口可用。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void threadEndpointShouldReturnThreadWeapon() throws Exception {
        mockMvc.perform(get("/demos/concurrency/thread"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weapon").value("Thread"))
            .andExpect(jsonPath("$.tasks[0].taskName").exists());
    }

    /**
     * 验证线程池实验接口可用。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void threadPoolEndpointShouldReturnThreadPoolWeapon() throws Exception {
        mockMvc.perform(get("/demos/concurrency/thread-pool"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weapon").value("ThreadPoolExecutor"))
            .andExpect(jsonPath("$.tasks[2].taskName").value("recommendations"));
    }

    /**
     * 验证 Spring 异步实验接口可用，
     * 并且线程名来自我们配置的异步线程池。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void springAsyncEndpointShouldReturnSpringAsyncWeapon() throws Exception {
        mockMvc.perform(get("/demos/concurrency/spring-async"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weapon").value("Spring @Async"))
            .andExpect(jsonPath("$.tasks[0].threadName").value(org.hamcrest.Matchers.startsWith("spring-async-")));
    }

    /**
     * 验证虚拟线程实验接口可用。
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void virtualThreadEndpointShouldReturnVirtualThreadWeapon() throws Exception {
        mockMvc.perform(get("/demos/concurrency/virtual-thread"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weapon").value("VirtualThread"));
    }
}
