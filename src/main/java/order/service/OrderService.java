package order.service;

import java.math.BigDecimal;
import java.util.List;

import customers.entity.Customer;
import customers.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import order.dto.OrderRequest;
import order.dto.OrderResponse;
import order.entity.Order;
import order.entity.OrderStatus;
import order.event.OrderCreatedEvent;
import order.kafka.OrderProducer;
import order.mapper.OrderMapper;
import order.repository.OrderRepository;
import product.entity.Product;
import product.repository.ProductRepository;
import redis.service.CacheService;
import shared.exception.InvalidOrderStatusException;
import shared.exception.NotFoundException;

@ApplicationScoped
@RequiredArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
public class OrderService {

    private final OrderMapper orderMapper;

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    // KAFKA
    private final OrderProducer orderProducer;

    // REDIS
    private final CacheService cacheService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Guardar en la base de datos
        Customer customer = customerRepository.findByIdOptional(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        Product product = productRepository.findByIdOptional(request.productId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Order order = Order.builder()
                .customer(customer)
                .product(product)
                .quantity(request.quantity())
                .status(OrderStatus.PENDING)
                .totalAmount(
                        product.getPrice()
                                .multiply(BigDecimal.valueOf(request.quantity())))
                .build();

        orderRepository.persist(order);

        // Guardar en REDIS
        // REDIS
        OrderResponse orderResponse = orderMapper.toResponse(order);

        String redisKey = "order:" + order.getId();
        cacheService.guardarJson(redisKey, orderResponse);

        // Crear evento en kafka
        OrderCreatedEvent event = new OrderCreatedEvent(order.getId());

        orderProducer.send(event);

        return orderResponse;
    }

    public List<OrderResponse> getOrders() {
        return orderRepository.listAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {

        String redisKey = "order:" + id;

        OrderResponse orderCached = cacheService.obtenerJson(redisKey, OrderResponse.class);
        if (orderCached != null) {
            return orderCached;
        }

        Order order = orderRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Orden not found"));

        OrderResponse response = orderMapper.toResponse(order);

        cacheService.guardarJson(redisKey, response);

        return response;
    }

    @Transactional
    public OrderResponse completeOrder(Long id) {
        Order order = findOrder(id);

        if(order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Order is no longer pending and cannot be modified");
        }

        order.setStatus(OrderStatus.COMPLETED);

        OrderResponse response = orderMapper.toResponse(order);

        String redisKey = "order:" + order.getId();
        cacheService.guardarJson(redisKey, response);

        return response;
    }

    private Order findOrder(Long id) {
        return orderRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }
}
