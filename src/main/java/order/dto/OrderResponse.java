package order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import order.entity.OrderStatus;

public record OrderResponse(
    Long id,
    Integer quantity,
    BigDecimal totalAmount,
    OrderStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,

    Long customerId,
    String customerEmail,

    Long productId,
    String productName,
    BigDecimal productPrice
) {

}