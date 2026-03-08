package lab.backend.independentdemos.javaconcurrencylab.web;

import java.util.Map;

import lab.backend.independentdemos.javaconcurrencylab.model.WeaponDemoResponse;
import lab.backend.independentdemos.javaconcurrencylab.service.ConcurrencyDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 并发武器实验控制器。
 *
 * 这个控制器把不同并发实现方式统一暴露成 HTTP 接口，
 * 方便通过浏览器、curl 或调试器逐个观察返回结果。
 */
@RestController
@RequestMapping("/demos/concurrency")
public class ConcurrencyDemoController {

    private final ConcurrencyDemoService concurrencyDemoService;

    /**
     * 注入并发实验服务。
     *
     * @param concurrencyDemoService 并发实验服务
     */
    public ConcurrencyDemoController(ConcurrencyDemoService concurrencyDemoService) {
        this.concurrencyDemoService = concurrencyDemoService;
    }

    /**
     * 提供最小健康检查接口。
     *
     * @return 健康状态
     */
    @GetMapping("/healthz")
    public Map<String, Object> healthz() {
        return Map.of("status", "UP");
    }

    /**
     * 根据武器名称执行对应实验。
     *
     * @param weapon 武器名称
     * @return 对应武器的实验结果
     */
    @GetMapping("/{weapon}")
    public WeaponDemoResponse runDemo(@PathVariable String weapon) {
        return switch (weapon) {
            case "thread" -> concurrencyDemoService.runThreadDemo();
            case "future-task" -> concurrencyDemoService.runFutureTaskDemo();
            case "thread-pool" -> concurrencyDemoService.runThreadPoolDemo();
            case "completable-future" -> concurrencyDemoService.runCompletableFutureDemo();
            case "spring-async" -> concurrencyDemoService.runSpringAsyncDemo();
            case "virtual-thread" -> concurrencyDemoService.runVirtualThreadDemo();
            default -> throw new IllegalArgumentException("不支持的武器类型: " + weapon);
        };
    }
}
