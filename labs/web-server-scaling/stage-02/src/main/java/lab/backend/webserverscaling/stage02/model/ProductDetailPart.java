package lab.backend.webserverscaling.stage02.model;

/**
 * 表示商品详情聚合接口中的一个子任务结果。
 *
 * @param name 子任务名称
 * @param value 子任务返回值
 * @param durationMs 子任务自身耗时
 * @param threadName 执行该任务的线程名
 */
public record ProductDetailPart(
    String name,
    String value,
    long durationMs,
    String threadName
) {
}
