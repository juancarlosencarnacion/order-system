package order.kafka;

import order.event.OrderCreatedEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;


@ApplicationScoped
public class OrderProducer {

    @Inject
    @Channel("ordenes-out")
    Emitter<OrderCreatedEvent> emitter;

    public void send(OrderCreatedEvent event) {
        System.out.println("ENVIANDO EVENTO -> " + event);
        emitter.send(event);
    }
}
