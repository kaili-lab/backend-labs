package lab.backend.webserverscaling.stage03;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * stage-03 的 Spring Boot 启动类。
 *
 * 这一阶段正式把 PostgreSQL 引入主线，
 * 让瓶颈从纯应用内逻辑开始迁移到数据库访问层。
 */
@SpringBootApplication
public class WebServerScalingStage03Application {

    /**
     * 启动 stage-03 实验应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WebServerScalingStage03Application.class, args);
    }
}
