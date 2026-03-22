package lab.backend.mall.notification.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ntf_notification_task")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_no", nullable = false, unique = true, length = 64)
    private String taskNo;

    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    @Column(name = "task_type", nullable = false, length = 64)
    private String taskType;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "next_trigger_at", nullable = false)
    private OffsetDateTime nextTriggerAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected NotificationTask() {
    }

    public static NotificationTask createPending(String orderNo, String taskType) {
        NotificationTask task = new NotificationTask();
        task.taskNo = "NTF" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
        task.orderNo = orderNo;
        task.taskType = taskType;
        task.status = "PENDING";
        task.attemptCount = 0;
        task.nextTriggerAt = OffsetDateTime.now();
        return task;
    }

    public void markRunning() {
        this.status = "RUNNING";
        this.attemptCount = this.attemptCount + 1;
        this.lastError = null;
    }

    public void markSuccess() {
        this.status = "SUCCESS";
        this.lastError = null;
    }

    public void markRetry(String errorMessage, OffsetDateTime nextTriggerAt) {
        this.status = "PENDING";
        this.lastError = truncate(errorMessage);
        this.nextTriggerAt = nextTriggerAt;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    public String getTaskNo() {
        return taskNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getTaskType() {
        return taskType;
    }
}
