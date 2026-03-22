package lab.backend.mall.inventory.application.dto;

import java.math.BigDecimal;

public record ProductInventoryView(
        Long productId,
        Long categoryId,
        String categoryName,
        String productName,
        BigDecimal price,
        String productStatus,
        Integer stock
) {
}
