package lab.backend.mall.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "productId 不能为空")
        Long productId,
        @NotNull(message = "quantity 不能为空")
        @Min(value = 1, message = "quantity 必须大于 0")
        Integer quantity
) {
}
