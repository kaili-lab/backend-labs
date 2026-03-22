package lab.backend.mall.notification.application;

import lab.backend.mall.notification.application.dto.ClaimedNotificationTask;
import lab.backend.mall.notification.domain.model.NotificationTask;
import lab.backend.mall.notification.domain.repository.NotificationTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public void createPendingTask(String orderNo, String taskType) {
        notificationTaskRepository.save(NotificationTask.createPending(orderNo, taskType));
    }

    @Transactional
    public List<ClaimedNotificationTask> claimPendingTasks(int limit) {
        // 扫描与“标记 RUNNING”放在同一事务里，避免同实例下重复提交同一任务。
        List<NotificationTask> dueTasks = notificationTaskRepository
                .findTop20ByStatusAndNextTriggerAtLessThanEqualOrderByIdAsc("PENDING", OffsetDateTime.now());

        int actualLimit = Math.min(limit, dueTasks.size());
        List<NotificationTask> selectedTasks = dueTasks.subList(0, actualLimit);
        for (NotificationTask task : selectedTasks) {
            task.markRunning();
        }
        return selectedTasks.stream()
                .map(task -> new ClaimedNotificationTask(task.getTaskNo(), task.getOrderNo(), task.getTaskType()))
                .toList();
    }

    @Transactional
    public void markSuccess(String taskNo) {
        notificationTaskRepository.findByTaskNo(taskNo).ifPresent(NotificationTask::markSuccess);
    }

    @Transactional
    public void markRetry(String taskNo, String errorMessage) {
        notificationTaskRepository.findByTaskNo(taskNo)
                .ifPresent(task -> task.markRetry(errorMessage, OffsetDateTime.now().plusSeconds(30)));
    }
}
