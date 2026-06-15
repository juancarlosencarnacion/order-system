package product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import product.entity.ProductCategory;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        ProductCategory category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
