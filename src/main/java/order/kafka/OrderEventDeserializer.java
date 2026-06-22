package order.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import order.dto.OrderEvent;

public class OrderEventDeserializer extends ObjectMapperDeserializer<OrderEvent> {

    public OrderEventDeserializer() {
        super(OrderEvent.class);
    }
}
