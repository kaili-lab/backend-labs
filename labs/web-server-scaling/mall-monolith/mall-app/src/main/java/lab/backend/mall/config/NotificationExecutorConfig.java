package lab.backend.mall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class NotificationExecutorConfig {

    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        // V1 先给通知执行单独线程池，避免异步任务挤占 Web 请求线程。
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ntf-exec-");
        executor.initialize();
        return executor;
    }
}
