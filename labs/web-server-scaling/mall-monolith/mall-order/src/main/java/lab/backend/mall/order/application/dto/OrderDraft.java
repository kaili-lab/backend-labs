package lab.backend.mall.order.application.dto;

import java.math.BigDecimal;

public record OrderDraft(Long productId, String productName, BigDecimal productPrice, Integer quantity) {
}
