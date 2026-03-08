package lab.backend.independentdemos.javaconcurrencylab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 并发武器库实验项目启动类。
 *
 * 这里开启 `@EnableAsync`，
 * 是为了让 Spring 的 `@Async` 实验能够真正切到异步线程执行。
 */
@EnableAsync
@SpringBootApplication
public class JavaConcurrencyLabApplication {

    /**
     * 启动并发武器库实验项目。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(JavaConcurrencyLabApplication.class, args);
    }
}
