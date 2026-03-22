package lab.backend.mall.controller;

import lab.backend.mall.application.PaymentConfirmFacade;
import lab.backend.mall.application.dto.ConfirmPaymentResult;
import lab.backend.mall.common.api.ApiResponse;
import lab.backend.mall.controller.dto.ConfirmPaymentResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentConfirmFacade paymentConfirmFacade;

    public PaymentController(PaymentConfirmFacade paymentConfirmFacade) {
        this.paymentConfirmFacade = paymentConfirmFacade;
    }

    @PostMapping("/{orderNo}/confirm")
    public ApiResponse<ConfirmPaymentResponse> confirmPayment(@PathVariable String orderNo) {
        ConfirmPaymentResult result = paymentConfirmFacade.confirmPayment(orderNo);
        ConfirmPaymentResponse response = new ConfirmPaymentResponse(
                result.orderNo(),
                result.orderStatus(),
                result.paymentStatus()
        );
        return ApiResponse.success(response);
    }
}
