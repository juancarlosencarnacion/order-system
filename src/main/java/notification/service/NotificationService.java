package notification.service;

import jakarta.enterprise.context.ApplicationScoped;
import order.event.OrderCreatedEvent;

@ApplicationScoped
public class NotificationService {

    public void sendOrderCreateNotidication(OrderCreatedEvent event) {
        System.out.println(
                "Notificando orden "
                        + event.orderId());
    }
}
