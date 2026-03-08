package lab.backend.webserverscaling.stage03.repository;

import lab.backend.webserverscaling.stage03.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 库存信息仓库。
 */
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
}
