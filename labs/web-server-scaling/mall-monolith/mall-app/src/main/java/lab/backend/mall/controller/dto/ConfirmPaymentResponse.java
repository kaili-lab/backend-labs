package lab.backend.mall.controller.dto;

public record ConfirmPaymentResponse(String orderNo, String orderStatus, String paymentStatus) {
}
