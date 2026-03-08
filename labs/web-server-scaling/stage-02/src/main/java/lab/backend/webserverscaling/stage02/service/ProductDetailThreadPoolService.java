package lab.backend.webserverscaling.stage02.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

import lab.backend.webserverscaling.stage02.model.PooledTaskResult;
import lab.backend.webserverscaling.stage02.model.ProductDetailAggregateResponse;
import lab.backend.webserverscaling.stage02.model.ProductDetailPart;
import lab.backend.webserverscaling.stage02.model.ThreadPoolBatchResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 使用受控线程池执行聚合任务的服务。
 *
 * 这个服务是 stage-02 的核心：
 * 它显式使用专用线程池执行任务，强调线程池解决的是资源控制和可预测性。
 */
@Service
public class ProductDetailThreadPoolService {

    private final Executor productAggregationExecutor;

    /**
     * 注入商品聚合专用线程池。
     *
     * @param productAggregationExecutor 商品聚合执行器
     */
    public ProductDetailThreadPoolService(
        @Qualifier("productAggregationExecutor") Executor productAggregationExecutor
    ) {
        this.productAggregationExecutor = productAggregationExecutor;
    }

    /**
     * 使用受控线程池聚合商品详情。
     *
     * @param productId 商品 ID
     * @return 聚合结果
     */
    public ProductDetailAggregateResponse getDetailsWithThreadPool(long productId) {
        long startedAt = System.currentTimeMillis();

        CompletableFuture<ProductDetailPart> productInfoFuture =
            CompletableFuture.supplyAsync(() -> fetchProductInfo(productId), productAggregationExecutor);
        CompletableFuture<ProductDetailPart> inventoryFuture =
            CompletableFuture.supplyAsync(() -> fetchInventory(productId), productAggregationExecutor);
        CompletableFuture<ProductDetailPart> recommendationsFuture =
            CompletableFuture.supplyAsync(() -> fetchRecommendations(productId), productAggregationExecutor);

        CompletableFuture.allOf(productInfoFuture, inventoryFuture, recommendationsFuture).join();

        return new ProductDetailAggregateResponse(
            productId,
            "thread-pool",
            System.currentTimeMillis() - startedAt,
            orderedParts(productInfoFuture.join(), inventoryFuture.join(), recommendationsFuture.join())
        );
    }

    /**
     * 提交一批独立任务到受控线程池，
     * 用来观察线程复用和排队现象。
     *
     * @param taskCount 任务数量
     * @param delayMs 每个任务耗时
     * @return 批量任务实验结果
     */
    public ThreadPoolBatchResponse runBatchExperiment(int taskCount, long delayMs) {
        long startedAt = System.currentTimeMillis();

        // 这里把大量任务统一提交到一个小线程池中，
        // 是为了让“线程有限、任务排队、线程复用”三个现象同时可见。
        List<CompletableFuture<PooledTaskResult>> futures = IntStream.rangeClosed(1, taskCount)
            .mapToObj(index -> CompletableFuture.supplyAsync(
                () -> runPooledTask("task-" + index, delayMs),
                productAggregationExecutor
            ))
            .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        return new ThreadPoolBatchResponse(
            "thread-pool-batch",
            taskCount,
            delayMs,
            System.currentTimeMillis() - startedAt,
            futures.stream().map(CompletableFuture::join).toList()
        );
    }

    /**
     * 用固定顺序组装聚合结果。
     *
     * @param productInfo 商品信息结果
     * @param inventory 库存结果
     * @param recommendations 推荐结果
     * @return 按顺序组织的子任务结果
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
     * 模拟商品信息查询。
     *
     * @param productId 商品 ID
     * @return 商品信息结果
     */
    private ProductDetailPart fetchProductInfo(long productId) {
        return runProductTask("productInfo", "商品-" + productId, 300L);
    }

    /**
     * 模拟库存查询。
     *
     * @param productId 商品 ID
     * @return 库存结果
     */
    private ProductDetailPart fetchInventory(long productId) {
        return runProductTask("inventory", "库存充足-" + productId, 300L);
    }

    /**
     * 模拟推荐查询。
     *
     * @param productId 商品 ID
     * @return 推荐结果
     */
    private ProductDetailPart fetchRecommendations(long productId) {
        return runProductTask("recommendations", "推荐商品列表-" + productId, 300L);
    }

    /**
     * 统一执行一个商品聚合子任务。
     *
     * @param taskName 任务名称
     * @param value 返回值
     * @param delayMs 模拟耗时
     * @return 子任务结果
     */
    private ProductDetailPart runProductTask(String taskName, String value, long delayMs) {
        long startedAt = System.currentTimeMillis();
        sleep(delayMs);
        return new ProductDetailPart(
            taskName,
            value,
            System.currentTimeMillis() - startedAt,
            Thread.currentThread().getName()
        );
    }

    /**
     * 统一执行批量实验中的任务。
     *
     * @param taskName 任务名称
     * @param delayMs 模拟耗时
     * @return 批量任务结果
     */
    private PooledTaskResult runPooledTask(String taskName, long delayMs) {
        long startedAt = System.currentTimeMillis();
        sleep(delayMs);
        return new PooledTaskResult(
            taskName,
            Thread.currentThread().getName(),
            System.currentTimeMillis() - startedAt
        );
    }

    /**
     * 统一模拟阻塞型耗时操作。
     *
     * @param millis 休眠毫秒数
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("线程被中断，无法完成当前模拟任务", exception);
        }
    }
}
