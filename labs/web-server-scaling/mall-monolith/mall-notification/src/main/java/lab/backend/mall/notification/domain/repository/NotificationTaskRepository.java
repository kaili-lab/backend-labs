package lab.backend.mall.notification.domain.repository;

import lab.backend.mall.notification.domain.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findTop20ByStatusAndNextTriggerAtLessThanEqualOrderByIdAsc(String status, OffsetDateTime triggerTime);

    Optional<NotificationTask> findByTaskNo(String taskNo);
}
