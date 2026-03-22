package lab.backend.mall.scheduler;

import lab.backend.mall.notification.application.NotificationSender;
import lab.backend.mall.notification.application.NotificationTaskService;
import lab.backend.mall.notification.application.dto.ClaimedNotificationTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

@Component
public class NotificationTaskScheduler {

    private final NotificationTaskService notificationTaskService;
    private final NotificationSender notificationSender;
    private final Executor notificationExecutor;

    public NotificationTaskScheduler(NotificationTaskService notificationTaskService,
                                     NotificationSender notificationSender,
                                     @Qualifier("notificationExecutor") Executor notificationExecutor) {
        this.notificationTaskService = notificationTaskService;
        this.notificationSender = notificationSender;
        this.notificationExecutor = notificationExecutor;
    }

    @Scheduled(fixedDelayString = "${mall.notification.scan-delay-ms:3000}")
    public void scanAndDispatch() {
        // 扫描只做“领任务+投递执行”，把真正发送放到线程池，避免调度线程被慢任务阻塞。
        List<ClaimedNotificationTask> claimedTasks = notificationTaskService.claimPendingTasks(10);
        for (ClaimedNotificationTask task : claimedTasks) {
            notificationExecutor.execute(() -> execute(task));
        }
    }

    private void execute(ClaimedNotificationTask task) {
        try {
            notificationSender.sendOrderPaid(task.orderNo());
            notificationTaskService.markSuccess(task.taskNo());
        } catch (Exception ex) {
            notificationTaskService.markRetry(task.taskNo(), ex.getMessage());
        }
    }
}
