package lab.backend.webserverscaling.stage02.model;

/**
 * 表示线程池批量实验中的单个任务结果。
 *
 * @param taskName 任务名称
 * @param threadName 执行线程名
 * @param durationMs 任务耗时
 */
public record PooledTaskResult(
    String taskName,
    String threadName,
    long durationMs
) {
}
