package lab.backend.webserverscaling.stage03.repository;

import lab.backend.webserverscaling.stage03.entity.RecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 推荐信息仓库。
 */
public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {
}
