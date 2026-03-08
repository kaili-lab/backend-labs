package lab.backend.webserverscaling.stage02.model;

import java.util.Map;

/**
 * 表示商品详情聚合接口响应。
 *
 * @param productId 商品 ID
 * @param strategy 当前策略名称
 * @param totalDurationMs 聚合总耗时
 * @param parts 各子任务结果
 */
public record ProductDetailAggregateResponse(
    long productId,
    String strategy,
    long totalDurationMs,
    Map<String, ProductDetailPart> parts
) {
}
