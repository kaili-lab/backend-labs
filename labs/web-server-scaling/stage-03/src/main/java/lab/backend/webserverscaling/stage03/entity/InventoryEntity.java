package lab.backend.webserverscaling.stage03.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 库存信息实体。
 */
@Entity
@Table(name = "inventories")
public class InventoryEntity {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private Integer available;

    public Long getProductId() {
        return productId;
    }

    public Integer getAvailable() {
        return available;
    }
}
