package lab.backend.independentdemos.javaconcurrencylab.service;

import java.util.concurrent.CompletableFuture;

import lab.backend.independentdemos.javaconcurrencylab.model.WeaponTaskResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Spring `@Async` 实验服务。
 *
 * 这个服务单独拆出来，是为了让 `@Async` 通过 Spring 代理生效。
 * 如果把异步方法和调用方写在同一个类里自调用，实验会失真。
 */
@Service
public class SpringAsyncDemoService {

    /**
     * 在 Spring 管理的线程池中异步执行一个任务。
     *
     * @param taskName 任务名称
     * @param delayMs 模拟耗时
     * @return 异步任务结果
     */
    @Async("mailLikeTaskExecutor")
    public CompletableFuture<WeaponTaskResult> runTask(String taskName, long delayMs) {
        long startedAt = System.currentTimeMillis();
        sleep(delayMs);
        return CompletableFuture.completedFuture(
            new WeaponTaskResult(
                taskName,
                Thread.currentThread().getName(),
                System.currentTimeMillis() - startedAt,
                taskName + " 完成"
            )
        );
    }

    /**
     * 统一模拟阻塞型任务耗时。
     *
     * @param millis 休眠毫秒数
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            // 异步任务被中断时，需要保留中断标记，
            // 以便外层能够感知线程状态已经发生变化。
            Thread.currentThread().interrupt();
            throw new IllegalStateException("异步任务被中断", exception);
        }
    }
}
