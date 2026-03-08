package lab.backend.webserverscaling.stage02.web;

import java.util.Map;

import lab.backend.webserverscaling.stage02.model.ProductDetailAggregateResponse;
import lab.backend.webserverscaling.stage02.model.ThreadPoolBatchResponse;
import lab.backend.webserverscaling.stage02.service.ProductDetailThreadPoolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * stage-02 控制器。
 *
 * 这里提供：
 * - 商品详情聚合接口
 * - 线程池批量任务观察接口
 */
@RestController
public class ProductDetailThreadPoolController {

    private final ProductDetailThreadPoolService productDetailThreadPoolService;

    /**
     * 注入 stage-02 业务服务。
     *
     * @param productDetailThreadPoolService stage-02 服务
     */
    public ProductDetailThreadPoolController(ProductDetailThreadPoolService productDetailThreadPoolService) {
        this.productDetailThreadPoolService = productDetailThreadPoolService;
    }

    /**
     * 提供最小健康检查接口。
     *
     * @return 健康状态
     */
    @GetMapping("/healthz")
    public Map<String, Object> healthz() {
        return Map.of("status", "UP");
    }

    /**
     * 使用受控线程池聚合商品详情。
     *
     * @param productId 商品 ID
     * @return 聚合结果
     */
    @GetMapping("/products/{productId}/details/thread-pool")
    public ProductDetailAggregateResponse getDetailsWithThreadPool(@PathVariable long productId) {
        return productDetailThreadPoolService.getDetailsWithThreadPool(productId);
    }

    /**
     * 观察线程池复用与任务排队。
     *
     * @param taskCount 任务数量
     * @param delayMs 每个任务耗时
     * @return 批量实验结果
     */
    @GetMapping("/experiments/thread-pool/batch")
    public ThreadPoolBatchResponse runBatchExperiment(
        @RequestParam(defaultValue = "6") int taskCount,
        @RequestParam(defaultValue = "200") long delayMs
    ) {
        return productDetailThreadPoolService.runBatchExperiment(taskCount, delayMs);
    }
}
