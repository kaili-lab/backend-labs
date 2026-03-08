package lab.backend.webserverscaling.stage03.web;

import java.util.Map;

import lab.backend.webserverscaling.stage03.model.ProductDetailAggregateResponse;
import lab.backend.webserverscaling.stage03.model.RepeatedReadResponse;
import lab.backend.webserverscaling.stage03.service.ProductDetailDatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * stage-03 控制器。
 *
 * 提供：
 * - 数据库版商品详情接口
 * - 重复读取实验接口
 */
@RestController
public class ProductDetailDatabaseController {

    private final ProductDetailDatabaseService productDetailDatabaseService;

    /**
     * 注入数据库读取服务。
     *
     * @param productDetailDatabaseService 数据库读取服务
     */
    public ProductDetailDatabaseController(ProductDetailDatabaseService productDetailDatabaseService) {
        this.productDetailDatabaseService = productDetailDatabaseService;
    }

    /**
     * 健康检查接口。
     *
     * @return 健康状态
     */
    @GetMapping("/healthz")
    public Map<String, Object> healthz() {
        return Map.of("status", "UP");
    }

    /**
     * 数据库版商品详情聚合接口。
     *
     * @param productId 商品 ID
     * @return 聚合结果
     */
    @GetMapping("/products/{productId}/details/database")
    public ProductDetailAggregateResponse getDetailsFromDatabase(@PathVariable long productId) {
        return productDetailDatabaseService.getDetailsFromDatabase(productId);
    }

    /**
     * 数据库重复读取实验接口。
     *
     * @param productId 商品 ID
     * @param times 重复次数
     * @return 实验结果
     */
    @GetMapping("/experiments/database/repeated-read")
    public RepeatedReadResponse runRepeatedRead(
        @RequestParam(defaultValue = "1") long productId,
        @RequestParam(defaultValue = "5") int times
    ) {
        return productDetailDatabaseService.runRepeatedRead(productId, times);
    }
}
