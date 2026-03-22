package lab.backend.mall.order.application.dto;

import java.math.BigDecimal;

public record OrderCreatedResult(String orderNo, String orderStatus, BigDecimal totalAmount) {
}
