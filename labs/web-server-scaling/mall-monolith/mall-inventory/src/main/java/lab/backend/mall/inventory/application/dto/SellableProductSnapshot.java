package lab.backend.mall.inventory.application.dto;

import java.math.BigDecimal;

public record SellableProductSnapshot(Long productId, String productName, BigDecimal productPrice) {
}
