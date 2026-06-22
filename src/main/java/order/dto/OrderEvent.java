package order.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        OrderResponse data

) {
    public OrderEvent(String eventType, OrderResponse data) {
        this(UUID.randomUUID().toString(), eventType, LocalDateTime.now(), data);
    }
}
