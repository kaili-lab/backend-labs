package lab.backend.webserverscaling.stage01.model;

/**
 * 表示商品详情聚合接口中的一个子任务结果。
 *
 * 这里故意把每个子任务都抽象成统一结构，
 * 是为了让串行版和并发版的返回结果可以直接对照。
 *
 * @param name 子任务名称
 * @param value 子任务返回值
 * @param durationMs 子任务自身耗时
 */
public record ProductDetailPart(
    String name,
    String value,
    long durationMs
) {
}
