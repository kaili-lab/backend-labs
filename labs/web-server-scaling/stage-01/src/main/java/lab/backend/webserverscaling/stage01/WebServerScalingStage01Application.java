package lab.backend.webserverscaling.stage01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * stage-01 的 Spring Boot 启动类。
 *
 * 这个阶段的目标是演示：
 * 一个请求内部如果存在多个彼此独立的耗时任务，
 * 串行执行和并发执行会带来明显不同的总响应时间。
 */
@SpringBootApplication
public class WebServerScalingStage01Application {

    /**
     * 启动 stage-01 实验应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WebServerScalingStage01Application.class, args);
    }
}
