package lab.backend.webserverscaling.stage02.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 商品详情聚合线程池配置。
 *
 * 当前阶段故意使用一个很小的线程池，
 * 是为了让线程复用和排队现象更容易被观察出来。
 */
@Configuration
public class ProductAggregationExecutorConfig {

    /**
     * 定义商品聚合任务专用线程池。
     *
     * @return 聚合任务执行器
     */
    @Bean(name = "productAggregationExecutor")
    public Executor productAggregationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("product-aggregation-");
        executor.initialize();
        return executor;
    }
}
