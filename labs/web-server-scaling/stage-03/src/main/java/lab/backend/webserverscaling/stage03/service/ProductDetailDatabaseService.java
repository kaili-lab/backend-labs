package lab.backend.webserverscaling.stage03.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import lab.backend.webserverscaling.stage03.entity.InventoryEntity;
import lab.backend.webserverscaling.stage03.entity.ProductEntity;
import lab.backend.webserverscaling.stage03.entity.RecommendationEntity;
import lab.backend.webserverscaling.stage03.model.ProductDetailAggregateResponse;
import lab.backend.webserverscaling.stage03.model.ProductDetailPart;
import lab.backend.webserverscaling.stage03.model.RepeatedReadResponse;
import lab.backend.webserverscaling.stage03.repository.InventoryRepository;
import lab.backend.webserverscaling.stage03.repository.ProductRepository;
import lab.backend.webserverscaling.stage03.repository.RecommendationRepository;
import org.springframework.stereotype.Service;

/**
 * 商品详情数据库读取服务。
 *
 * 这个服务强调：
 * 从这一阶段开始，请求处理已经不再只是应用内逻辑，
 * 而是需要依赖数据库这一外部系统。
 */
@Service
public class ProductDetailDatabaseService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final RecommendationRepository recommendationRepository;

    /**
     * 注入商品详情相关仓库。
     *
     * @param productRepository 商品仓库
     * @param inventoryRepository 库存仓库
     * @param recommendationRepository 推荐仓库
     */
    public ProductDetailDatabaseService(
        ProductRepository productRepository,
        InventoryRepository inventoryRepository,
        RecommendationRepository recommendationRepository
    ) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.recommendationRepository = recommendationRepository;
    }

    /**
     * 聚合读取商品详情。
     *
     * @param productId 商品 ID
     * @return 数据库版聚合结果
     */
    public ProductDetailAggregateResponse getDetailsFromDatabase(long productId) {
        long startedAt = System.currentTimeMillis();

        ProductDetailPart productInfo = fetchProductInfo(productId);
        ProductDetailPart inventory = fetchInventory(productId);
        ProductDetailPart recommendations = fetchRecommendations(productId);

        return new ProductDetailAggregateResponse(
            productId,
            "database",
            System.currentTimeMillis() - startedAt,
            orderedParts(productInfo, inventory, recommendations)
        );
    }

    /**
     * 重复读取商品详情，
     * 用于观察数据库一旦进入链路，总成本会随着访问次数明显累积。
     *
     * @param productId 商品 ID
     * @param times 重复次数
     * @return 重复读取实验结果
     */
    public RepeatedReadResponse runRepeatedRead(long productId, int times) {
        long startedAt = System.currentTimeMillis();

        List<Long> durations = IntStream.range(0, times)
            .mapToObj(index -> {
                long singleStartedAt = System.currentTimeMillis();
                getDetailsFromDatabase(productId);
                return System.currentTimeMillis() - singleStartedAt;
            })
            .toList();

        return new RepeatedReadResponse(
            productId,
            times,
            System.currentTimeMillis() - startedAt,
            durations
        );
    }

    /**
     * 从数据库读取商品信息。
     *
     * @param productId 商品 ID
     * @return 商品信息结果
     */
    private ProductDetailPart fetchProductInfo(long productId) {
        long startedAt = System.currentTimeMillis();
        ProductEntity product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("商品不存在: " + productId));
        return new ProductDetailPart(
            "productInfo",
            product.getName() + " - " + product.getDescription(),
            System.currentTimeMillis() - startedAt
        );
    }

    /**
     * 从数据库读取库存信息。
     *
     * @param productId 商品 ID
     * @return 库存信息结果
     */
    private ProductDetailPart fetchInventory(long productId) {
        long startedAt = System.currentTimeMillis();
        InventoryEntity inventory = inventoryRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("库存不存在: " + productId));
        return new ProductDetailPart(
            "inventory",
            "可用库存: " + inventory.getAvailable(),
            System.currentTimeMillis() - startedAt
        );
    }

    /**
     * 从数据库读取推荐信息。
     *
     * @param productId 商品 ID
     * @return 推荐信息结果
     */
    private ProductDetailPart fetchRecommendations(long productId) {
        long startedAt = System.currentTimeMillis();
        RecommendationEntity recommendation = recommendationRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("推荐信息不存在: " + productId));
        return new ProductDetailPart(
            "recommendations",
            recommendation.getSummary(),
            System.currentTimeMillis() - startedAt
        );
    }

    /**
     * 用固定顺序组装聚合结果。
     *
     * @param productInfo 商品信息结果
     * @param inventory 库存结果
     * @param recommendations 推荐结果
     * @return 聚合结果映射
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
}
