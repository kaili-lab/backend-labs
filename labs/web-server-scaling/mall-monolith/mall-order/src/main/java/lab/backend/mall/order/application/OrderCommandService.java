package lab.backend.mall.order.application;

import lab.backend.mall.common.exception.BusinessException;
import lab.backend.mall.order.application.dto.OrderCreatedResult;
import lab.backend.mall.order.application.dto.OrderDraft;
import lab.backend.mall.order.domain.model.Order;
import lab.backend.mall.order.domain.model.OrderItem;
import lab.backend.mall.order.domain.repository.OrderItemRepository;
import lab.backend.mall.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderCommandService(OrderRepository orderRepository,
                               OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public OrderCreatedResult createPendingOrder(OrderDraft draft) {
        // 下单时写入商品快照，避免后续商品改名/改价影响历史订单可追溯性。
        BigDecimal totalAmount = draft.productPrice().multiply(BigDecimal.valueOf(draft.quantity()));
        String orderNo = generateOrderNo();

        Order order = Order.createPendingPayment(orderNo, totalAmount);
        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = OrderItem.create(
                orderNo,
                draft.productId(),
                draft.productName(),
                draft.productPrice(),
                draft.quantity(),
                totalAmount
        );
        orderItemRepository.save(orderItem);

        return new OrderCreatedResult(savedOrder.getOrderNo(), savedOrder.getStatus(), savedOrder.getTotalAmount());
    }

    public Order markOrderPaid(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "订单不存在", 404));
        // 先做最小状态约束，避免重复确认导致状态乱序；完整幂等策略放到 V2。
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("ORDER_STATUS_INVALID", "订单状态不允许确认支付", 400);
        }
        order.markPaid();
        return order;
    }

    private String generateOrderNo() {
        return "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}
