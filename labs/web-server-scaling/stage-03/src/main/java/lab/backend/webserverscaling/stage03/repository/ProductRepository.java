package lab.backend.webserverscaling.stage03.repository;

import lab.backend.webserverscaling.stage03.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 商品基础信息仓库。
 */
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
