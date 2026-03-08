package lab.backend.webserverscaling.stage01.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lab.backend.webserverscaling.stage01.model.ProductDetailAggregateResponse;
import lab.backend.webserverscaling.stage01.model.ProductDetailPart;
import org.springframework.stereotype.Service;

/**
 * 商品详情聚合服务。
 *
 * 这个服务是 stage-01 的核心：
 * 它用同一组子任务，分别演示串行执行和并发执行两种策略，
 * 从而让“并发为什么有价值”变得可观察。
 */
@Service
public class ProductDetailService {

    /**
     * 使用串行方式聚合商品详情。
     *
     * 这里故意按顺序逐个执行子任务，
     * 是为了构造最直接的对照组。
     *
     * @param productId 商品 ID
     * @return 串行聚合后的响应
     */
    public ProductDetailAggregateResponse getDetailsSerial(long productId) {
        long startedAt = System.currentTimeMillis();

        ProductDetailPart productInfo = fetchProductInfo(productId);
        ProductDetailPart inventory = fetchInventory(productId);
        ProductDetailPart recommendations = fetchRecommendations(productId);

        return new ProductDetailAggregateResponse(
            productId,
            "serial",
            System.currentTimeMillis() - startedAt,
            orderedParts(productInfo, inventory, recommendations)
        );
    }

    /**
     * 使用并发方式聚合商品详情。
     *
     * 当前阶段故意使用最直接的 `CompletableFuture.supplyAsync(...)`，
     * 目的是先证明并发能降低总响应时间。
     * 线程池和资源控制的问题，留到下一阶段再展开。
     *
     * @param productId 商品 ID
     * @return 并发聚合后的响应
     */
    public ProductDetailAggregateResponse getDetailsConcurrent(long productId) {
        long startedAt = System.currentTimeMillis();

        // 这里把三个彼此独立的子任务同时发出去，
        // 让总耗时接近“最慢的那个任务”，而不是三个任务耗时之和。
        CompletableFuture<ProductDetailPart> productInfoFuture =
            CompletableFuture.supplyAsync(() -> fetchProductInfo(productId));
        CompletableFuture<ProductDetailPart> inventoryFuture =
            CompletableFuture.supplyAsync(() -> fetchInventory(productId));
        CompletableFuture<ProductDetailPart> recommendationsFuture =
            CompletableFuture.supplyAsync(() -> fetchRecommendations(productId));

        // 先等待所有任务结束，再统一汇总结果，
        // 这样返回给调用方的仍然是一个完整聚合后的响应。
        CompletableFuture.allOf(productInfoFuture, inventoryFuture, recommendationsFuture).join();

        return new ProductDetailAggregateResponse(
            productId,
            "concurrent",
            System.currentTimeMillis() - startedAt,
            orderedParts(productInfoFuture.join(), inventoryFuture.join(), recommendationsFuture.join())
        );
    }

    /**
     * 以固定顺序组装子任务结果。
     *
     * 使用 `LinkedHashMap` 是为了保持返回结果顺序稳定，
     * 这样在阅读返回 JSON 时更容易对照观察。
     *
     * @param productInfo 商品信息结果
     * @param inventory 库存结果
     * @param recommendations 推荐结果
     * @return 按固定顺序组织后的结果集合
     */
    private Map<String, ProductDetailPart> orderedParts(
        ProductDetailPart productInfo,
        ProductDetailPart inventory,
        ProductDetailPart recommendations
    ) {
        Map<String, ProductDetailPart> parts = new LinkedHashMap<>();
        parts.put("productInfo", productInfo);
        parts.put("inventory", inventory);
        parts.put("recommendations", recommendations);
        return parts;
    }

    /**
     * 模拟查询商品基本信息。
     *
     * @param productId 商品 ID
     * @return 商品信息结果
     */
    private ProductDetailPart fetchProductInfo(long productId) {
        sleep(300L);
        return new ProductDetailPart("productInfo", "商品-" + productId, 300L);
    }

    /**
     * 模拟查询库存信息。
     *
     * @param productId 商品 ID
     * @return 库存结果
     */
    private ProductDetailPart fetchInventory(long productId) {
        sleep(300L);
        return new ProductDetailPart("inventory", "库存充足-" + productId, 300L);
    }

    /**
     * 模拟查询推荐信息。
     *
     * @param productId 商品 ID
     * @return 推荐结果
     */
    private ProductDetailPart fetchRecommendations(long productId) {
        sleep(300L);
        return new ProductDetailPart("recommendations", "推荐商品列表-" + productId, 300L);
    }

    /**
     * 统一模拟阻塞型耗时操作。
     *
     * 这里使用 `Thread.sleep(...)` 是为了在不接入数据库和外部服务的前提下，
     * 人为构造一个可重复观察的耗时任务。
     *
     * @param millis 休眠毫秒数
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            // 一旦中断，既要恢复中断标记，
            // 也要让上层知道当前模拟任务已经失败。
            Thread.currentThread().interrupt();
            throw new IllegalStateException("线程被中断，无法完成当前模拟任务", exception);
        }
    }
}
