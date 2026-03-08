package lab.backend.webserverscaling.stage03.model;

import java.util.Map;

/**
 * 商品详情数据库聚合响应。
 *
 * @param productId 商品 ID
 * @param strategy 当前策略名称
 * @param totalDurationMs 聚合总耗时
 * @param parts 聚合子任务结果
 */
public record ProductDetailAggregateResponse(
    long productId,
    String strategy,
    long totalDurationMs,
    Map<String, ProductDetailPart> parts
) {
}
