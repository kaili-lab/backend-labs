package lab.backend.webserverscaling.stage03.model;

/**
 * 商品详情聚合中的单个数据库子查询结果。
 *
 * @param name 子任务名称
 * @param value 子任务结果
 * @param durationMs 子任务耗时
 */
public record ProductDetailPart(
    String name,
    String value,
    long durationMs
) {
}
