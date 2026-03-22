package lab.backend.mall.application.dto;

public record CreateOrderCommand(Long productId, Integer quantity) {
}
