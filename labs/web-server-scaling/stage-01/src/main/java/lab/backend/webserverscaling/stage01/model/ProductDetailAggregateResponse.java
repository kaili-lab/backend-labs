package lab.backend.webserverscaling.stage01.model;

import java.util.Map;

/**
 * 表示商品详情聚合接口的统一响应。
 *
 * 这个响应里同时保留：
 * - 当前商品 ID
 * - 使用的实现策略
 * - 总耗时
 * - 每个子任务结果
 *
 * 这样设计是为了让学习者可以直接观察“整体耗时”和“局部任务”之间的关系。
 *
 * @param productId 商品 ID
 * @param strategy 当前使用的聚合策略
 * @param totalDurationMs 聚合总耗时
 * @param parts 每个子任务的结果集合
 */
public record ProductDetailAggregateResponse(
    long productId,
    String strategy,
    long totalDurationMs,
    Map<String, ProductDetailPart> parts
) {
}
