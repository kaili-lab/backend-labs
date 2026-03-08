package lab.backend.webserverscaling.stage02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * stage-02 的 Spring Boot 启动类。
 *
 * 这个阶段从“并发有价值”继续推进到“并发必须被控制”。
 */
@SpringBootApplication
public class WebServerScalingStage02Application {

    /**
     * 启动 stage-02 实验应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WebServerScalingStage02Application.class, args);
    }
}
