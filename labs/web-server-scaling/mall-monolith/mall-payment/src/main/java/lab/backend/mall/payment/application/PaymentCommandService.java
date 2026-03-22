package lab.backend.mall.payment.application;

import lab.backend.mall.common.exception.BusinessException;
import lab.backend.mall.payment.application.dto.PaymentInitResult;
import lab.backend.mall.payment.domain.model.Payment;
import lab.backend.mall.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;

    public PaymentCommandService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentInitResult initializePendingPayment(String orderNo, BigDecimal amount) {
        // 创建订单时只初始化支付记录，不在这里做真实扣款，保持 V1 链路最小闭环。
        String paymentNo = generatePaymentNo();
        Payment payment = Payment.createPending(paymentNo, orderNo, amount);
        Payment savedPayment = paymentRepository.save(payment);
        return new PaymentInitResult(savedPayment.getPaymentNo(), savedPayment.getStatus());
    }

    public Payment markPaymentSuccess(String orderNo) {
        Payment payment = paymentRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "支付记录不存在", 404));
        // 这里先限制为 PENDING->SUCCESS，避免重复确认把错误状态覆盖掉。
        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessException("PAYMENT_STATUS_INVALID", "支付状态不允许确认", 400);
        }
        payment.markSuccess();
        return payment;
    }

    private String generatePaymentNo() {
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}
