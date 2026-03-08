package lab.backend.webserverscaling.stage03.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 推荐信息实体。
 */
@Entity
@Table(name = "recommendations")
public class RecommendationEntity {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private String summary;

    public Long getProductId() {
        return productId;
    }

    public String getSummary() {
        return summary;
    }
}
