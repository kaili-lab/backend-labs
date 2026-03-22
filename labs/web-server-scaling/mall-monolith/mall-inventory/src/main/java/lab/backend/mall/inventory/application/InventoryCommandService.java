package lab.backend.mall.inventory.application;

import lab.backend.mall.common.exception.BusinessException;
import lab.backend.mall.inventory.application.dto.SellableProductSnapshot;
import lab.backend.mall.inventory.domain.model.Inventory;
import lab.backend.mall.inventory.domain.model.Product;
import lab.backend.mall.inventory.domain.repository.InventoryRepository;
import lab.backend.mall.inventory.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryCommandService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public InventoryCommandService(ProductRepository productRepository,
                                   InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public SellableProductSnapshot loadSellableProduct(Long productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "商品不存在", 404));
        // 下单前先校验商品可售状态，避免把业务兜底放到库存扣减阶段。
        if (!"ON_SHELF".equals(product.getStatus())) {
            throw new BusinessException("PRODUCT_NOT_ON_SHELF", "商品当前不可下单", 400);
        }
        return new SellableProductSnapshot(product.getProductId(), product.getName(), product.getPrice());
    }

    public void deductStock(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException("INVENTORY_NOT_FOUND", "库存不存在", 404));
        // 这里用事务内原地扣减，先保证“正确性”，并发优化留到 V2 专门处理。
        if (inventory.getStock() < quantity) {
            throw new BusinessException("INSUFFICIENT_STOCK", "库存不足", 400);
        }
        inventory.decreaseStock(quantity);
    }
}
