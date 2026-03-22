package lab.backend.mall.application.dto;

public record ConfirmPaymentResult(String orderNo, String orderStatus, String paymentStatus) {
}
