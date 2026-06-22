package order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import order.entity.OrderStatus;
import product.entity.ProductCategory;

public record OrderResponse(
        // ORDER
        Long id,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // CUSTOMER
        Long customerId,
        String customerEmail,

        // ORDER DETAILS
        List<OrderDetails> details

) {
    public record OrderDetails(
            Long orderDetailId,
            Long productId,
            String productName,
            ProductCategory category,
            Integer quantity,
            BigDecimal priceAtPurchase) {
    }

}