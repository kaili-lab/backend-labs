package lab.backend.mall.controller;

import jakarta.validation.Valid;
import lab.backend.mall.application.OrderCreationFacade;
import lab.backend.mall.application.dto.CreateOrderCommand;
import lab.backend.mall.application.dto.CreateOrderResult;
import lab.backend.mall.common.api.ApiResponse;
import lab.backend.mall.controller.dto.CreateOrderRequest;
import lab.backend.mall.controller.dto.CreateOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCreationFacade orderCreationFacade;

    public OrderController(OrderCreationFacade orderCreationFacade) {
        this.orderCreationFacade = orderCreationFacade;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResult result = orderCreationFacade.createOrder(new CreateOrderCommand(
                request.productId(),
                request.quantity()
        ));
        CreateOrderResponse response = new CreateOrderResponse(
                result.orderNo(),
                result.orderStatus(),
                result.paymentStatus(),
                result.totalAmount()
        );
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }
}
