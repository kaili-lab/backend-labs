package lab.backend.mall.controller;

import lab.backend.mall.common.api.ApiResponse;
import lab.backend.mall.inventory.application.InventoryQueryService;
import lab.backend.mall.inventory.application.dto.ProductInventoryView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryQueryService inventoryQueryService;

    public InventoryController(InventoryQueryService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductInventoryView> getProduct(@PathVariable Long productId) {
        return ApiResponse.success(inventoryQueryService.getProduct(productId));
    }
}
