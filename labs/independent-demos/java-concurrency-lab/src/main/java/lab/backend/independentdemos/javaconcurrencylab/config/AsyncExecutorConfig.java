package lab.backend.independentdemos.javaconcurrencylab.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Spring 异步执行器配置。
 *
 * 这里单独定义一个具名线程池，
 * 是为了在实验里明确展示：
 * Spring 异步能力的背后，依然需要一个可控的执行器。
 */
@Configuration
public class AsyncExecutorConfig {

    /**
     * 定义一个模拟“发邮件/通知类任务”的线程池。
     *
     * 固定线程数和统一线程名前缀，
     * 方便在接口返回里直观看到线程池确实生效了。
     *
     * @return Spring 异步执行器
     */
    @Bean(name = "mailLikeTaskExecutor")
    public Executor mailLikeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("spring-async-");
        executor.initialize();
        return executor;
    }
}
