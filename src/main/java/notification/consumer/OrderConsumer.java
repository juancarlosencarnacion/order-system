package notification.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import notification.service.NotificationService;
import order.event.OrderCreatedEvent;

@ApplicationScoped
@RequiredArgsConstructor
public class OrderConsumer {

    private final NotificationService notificationService;

    @Incoming("ordenes-in")
    public void receive(OrderCreatedEvent event) {
        notificationService.sendOrderCreateNotidication(event);
    }
}
