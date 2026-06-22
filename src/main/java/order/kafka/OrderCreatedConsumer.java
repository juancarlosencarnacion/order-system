package order.kafka;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import order.dto.OrderEvent;
import order.service.OrderService;

@ApplicationScoped
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final OrderService orderService;
    private final OrderProducer orderProducer;

    @Incoming("order-created-in")
    public void consumeOrderCreated(OrderEvent event) {
        var orderData = event.data();

        // 1. Validar y descontar stock de forma atomica en la base de datos
        boolean stockReserved = orderService.verifyAndReserveStockIdempotent(orderData.details(), orderData.id());

        if (stockReserved) {
            orderProducer.sendOrderVerified(orderData);
        }
    }
}
