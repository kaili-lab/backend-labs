package lab.backend.mall.notification.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    public void sendOrderPaid(String orderNo) {
        // V1 仅保留最小可观察行为：先用日志模拟外部通知发送，避免提前引入外部通道复杂度。
        log.info("send order paid notification, orderNo={}", orderNo);
    }
}
