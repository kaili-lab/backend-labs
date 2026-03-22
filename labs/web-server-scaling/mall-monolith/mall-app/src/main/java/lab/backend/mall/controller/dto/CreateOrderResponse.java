package lab.backend.mall.controller.dto;

import java.math.BigDecimal;

public record CreateOrderResponse(String orderNo,
                                  String orderStatus,
                                  String paymentStatus,
                                  BigDecimal totalAmount) {
}
