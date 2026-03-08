package lab.backend.webserverscaling.stage01.web;

import lab.backend.webserverscaling.stage01.model.ProductDetailAggregateResponse;
import lab.backend.webserverscaling.stage01.service.ProductDetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品详情聚合接口控制器。
 *
 * 这个控制器提供两个入口：
 * - 串行版
 * - 并发版
 *
 * 让使用者可以通过同一个业务语义直接比较两种实现策略。
 */
@RestController
@RequestMapping("/products")
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    /**
     * 注入商品详情聚合服务。
     *
     * @param productDetailService 商品详情聚合服务
     */
    public ProductDetailController(ProductDetailService productDetailService) {
        this.productDetailService = productDetailService;
    }

    /**
     * 调用串行聚合版本。
     *
     * @param productId 商品 ID
     * @return 串行版本响应
     */
    @GetMapping("/{productId}/details/serial")
    public ProductDetailAggregateResponse getDetailsSerial(@PathVariable long productId) {
        return productDetailService.getDetailsSerial(productId);
    }

    /**
     * 调用并发聚合版本。
     *
     * @param productId 商品 ID
     * @return 并发版本响应
     */
    @GetMapping("/{productId}/details/concurrent")
    public ProductDetailAggregateResponse getDetailsConcurrent(@PathVariable long productId) {
        return productDetailService.getDetailsConcurrent(productId);
    }
}
