package order.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import order.dto.OrderEvent;
import order.dto.OrderResponse;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;


@ApplicationScoped
public class OrderProducer {

    @Inject
    @Channel("order-created-out")
    Emitter<OrderEvent> createdEmitter;

    @Inject
    @Channel("order-verified-out")
    Emitter<OrderEvent> verifiedEmitter;

    public void sendOrderCreated(OrderResponse orderData) {
        OrderEvent event = new OrderEvent(EventType.CREATED.name(), orderData);
        createdEmitter.send(event);
    }
    
    public void sendOrderVerified(OrderResponse orderData) {
        OrderEvent event = new OrderEvent(EventType.STOCK_VERIFIED.name(), orderData);
        verifiedEmitter.send(event);
    }
}
