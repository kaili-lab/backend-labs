package lab.backend.mall.application;

import lab.backend.mall.application.dto.CreateOrderCommand;
import lab.backend.mall.application.dto.CreateOrderResult;
import lab.backend.mall.common.exception.BusinessException;
import lab.backend.mall.inventory.application.InventoryCommandService;
import lab.backend.mall.inventory.application.dto.SellableProductSnapshot;
import lab.backend.mall.order.application.OrderCommandService;
import lab.backend.mall.order.application.dto.OrderCreatedResult;
import lab.backend.mall.order.application.dto.OrderDraft;
import lab.backend.mall.payment.application.PaymentCommandService;
import lab.backend.mall.payment.application.dto.PaymentInitResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCreationFacade {

    private final InventoryCommandService inventoryCommandService;
    private final OrderCommandService orderCommandService;
    private final PaymentCommandService paymentCommandService;

    public OrderCreationFacade(InventoryCommandService inventoryCommandService,
                               OrderCommandService orderCommandService,
                               PaymentCommandService paymentCommandService) {
        this.inventoryCommandService = inventoryCommandService;
        this.orderCommandService = orderCommandService;
        this.paymentCommandService = paymentCommandService;
    }

    @Transactional
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        if (command.quantity() == null || command.quantity() <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "购买数量必须大于 0", 400);
        }

        // 保持“库存扣减 + 订单写入 + 支付初始化”在同一事务，确保不会出现半成品订单。
        SellableProductSnapshot productSnapshot = inventoryCommandService.loadSellableProduct(command.productId());
        inventoryCommandService.deductStock(command.productId(), command.quantity());

        OrderCreatedResult orderResult = orderCommandService.createPendingOrder(new OrderDraft(
                productSnapshot.productId(),
                productSnapshot.productName(),
                productSnapshot.productPrice(),
                command.quantity()
        ));
        PaymentInitResult paymentResult = paymentCommandService.initializePendingPayment(
                orderResult.orderNo(),
                orderResult.totalAmount()
        );

        return new CreateOrderResult(
                orderResult.orderNo(),
                orderResult.orderStatus(),
                paymentResult.paymentStatus(),
                orderResult.totalAmount()
        );
    }
}
