package lab.backend.webserverscaling.stage02.model;

import java.util.List;

/**
 * 表示线程池批量任务观察接口的响应。
 *
 * @param strategy 当前实验策略
 * @param taskCount 提交任务数
 * @param delayMs 每个任务模拟耗时
 * @param totalDurationMs 整体实验耗时
 * @param tasks 各任务结果
 */
public record ThreadPoolBatchResponse(
    String strategy,
    int taskCount,
    long delayMs,
    long totalDurationMs,
    List<PooledTaskResult> tasks
) {
}
