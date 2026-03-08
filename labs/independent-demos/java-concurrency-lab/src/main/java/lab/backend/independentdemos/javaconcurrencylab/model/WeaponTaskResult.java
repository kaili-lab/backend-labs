package lab.backend.independentdemos.javaconcurrencylab.model;

/**
 * 表示单个并发任务的执行结果。
 *
 * @param taskName 任务名称
 * @param threadName 执行该任务的线程名称
 * @param durationMs 任务自身耗时
 * @param result 任务结果摘要
 */
public record WeaponTaskResult(
    String taskName,
    String threadName,
    long durationMs,
    String result
) {
}
