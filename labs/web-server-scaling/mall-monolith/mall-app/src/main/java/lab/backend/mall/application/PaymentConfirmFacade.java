package lab.backend.mall.application;

import lab.backend.mall.application.dto.ConfirmPaymentResult;
import lab.backend.mall.notification.application.NotificationTaskService;
import lab.backend.mall.order.application.OrderCommandService;
import lab.backend.mall.order.domain.model.Order;
import lab.backend.mall.payment.application.PaymentCommandService;
import lab.backend.mall.payment.domain.model.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentConfirmFacade {

    private final PaymentCommandService paymentCommandService;
    private final OrderCommandService orderCommandService;
    private final NotificationTaskService notificationTaskService;

    public PaymentConfirmFacade(PaymentCommandService paymentCommandService,
                                OrderCommandService orderCommandService,
                                NotificationTaskService notificationTaskService) {
        this.paymentCommandService = paymentCommandService;
        this.orderCommandService = orderCommandService;
        this.notificationTaskService = notificationTaskService;
    }

    @Transactional
    public ConfirmPaymentResult confirmPayment(String orderNo) {
        // 把支付状态、订单状态、通知任务入库放到同一事务，保证“支付成功”事实可被后续异步流程消费。
        Payment payment = paymentCommandService.markPaymentSuccess(orderNo);
        Order order = orderCommandService.markOrderPaid(orderNo);
        notificationTaskService.createPendingTask(orderNo, "ORDER_PAID");
        return new ConfirmPaymentResult(orderNo, order.getStatus(), payment.getStatus());
    }
}
