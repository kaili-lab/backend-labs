package lab.backend.mall.application.dto;

import java.math.BigDecimal;

public record CreateOrderResult(String orderNo,
                                String orderStatus,
                                String paymentStatus,
                                BigDecimal totalAmount) {
}
