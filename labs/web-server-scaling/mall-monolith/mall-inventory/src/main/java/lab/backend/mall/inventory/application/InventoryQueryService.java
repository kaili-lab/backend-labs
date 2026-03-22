package lab.backend.mall.inventory.application;

import lab.backend.mall.common.exception.BusinessException;
import lab.backend.mall.inventory.application.dto.ProductInventoryView;
import lab.backend.mall.inventory.domain.model.Category;
import lab.backend.mall.inventory.domain.model.Inventory;
import lab.backend.mall.inventory.domain.model.Product;
import lab.backend.mall.inventory.domain.repository.CategoryRepository;
import lab.backend.mall.inventory.domain.repository.InventoryRepository;
import lab.backend.mall.inventory.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryQueryService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;

    public InventoryQueryService(ProductRepository productRepository,
                                 InventoryRepository inventoryRepository,
                                 CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductInventoryView getProduct(Long productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "商品不存在", 404));

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException("INVENTORY_NOT_FOUND", "库存不存在", 404));

        Category category = categoryRepository.findByCategoryId(product.getCategoryId())
                .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "分类不存在", 404));

        return new ProductInventoryView(
                product.getProductId(),
                product.getCategoryId(),
                category.getName(),
                product.getName(),
                product.getPrice(),
                product.getStatus(),
                inventory.getStock()
        );
    }
}
