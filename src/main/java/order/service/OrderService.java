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
import order.dto.OrderResponse.OrderDetails;
import order.entity.Order;
import order.entity.OrderStatus;
import order.kafka.OrderProducer;
import order.mapper.OrderMapper;
import order.repository.OrderRepository;
import product.entity.Product;
import product.repository.ProductRepository;
import product.service.ProductService;
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
    private final ProductService productService;

    // KAFKA
    private final OrderProducer orderProducer;

    // REDIS
    private final CacheService cacheService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // Guardar en la base de datos
        Customer customer = customerRepository.findByIdOptional(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        Order newOrder = new Order();

        newOrder.setCustomer(customer);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderRequest.Products item : request.products()) {
            Product product = productRepository.findByIdOptional(item.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            newOrder.addProduct(product, item.quantity());

            BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(item.quantity()));

            totalAmount = totalAmount.add(total);
        }

        newOrder.setTotalAmount(totalAmount);
        newOrder.setStatus(OrderStatus.PENDING);
        orderRepository.persist(newOrder);

        OrderResponse response = orderMapper.toResponse(newOrder);

        // * Guardar en REDIS
        String redisKey = "order:" + response.id();
        cacheService.guardarJson(redisKey, response);

        // * Crear evento en kafka
        orderProducer.sendOrderCreated(response);

        return response;
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

        if (order.getStatus() != OrderStatus.PENDING) {
            System.out.println("ORDER STATUS: " + order.getStatus());
            System.out.println(order.getStatus().getClass().getSimpleName());
            System.out.println(OrderStatus.PENDING.getClass().getSimpleName());
            throw new InvalidOrderStatusException("Order is no longer pending and cannot be modified");
        }

        order.setStatus(OrderStatus.COMPLETED);

        OrderResponse response = orderMapper.toResponse(order);

        String redisKey = "order:" + order.getId();
        cacheService.guardarJson(redisKey, response);

        return response;
    }

    @Transactional
    public void rejectOrder(Long id) {
        Order order = findOrder(id);
        order.setStatus(OrderStatus.REJECTED);

        String redisKey = "order:" + id;
        cacheService.eliminar(redisKey);
    }

    @Transactional
    public boolean verifyAndReserveStockIdempotent(List<OrderDetails> details, Long orderId) {
        // 1. Validar el estado real de la orden en la BD (Evita duplicados)
        Order order = orderRepository.findByIdOptional(orderId)
                .orElseThrow(() -> new shared.exception.NotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return false; // Si ya no está PENDING, cancelamos el flujo de inmediato
        }

        // 2. Llamar a tu método actual de ProductService (comparte la misma
        // transacción)
        boolean stockReserved = productService.validateAndReserveStock(details);

        String redisKey = "order:" + orderId;

        if (stockReserved) {
            OrderResponse response = orderMapper.toResponse(order);
            cacheService.guardarJson(redisKey, response);
        }
        else if (!stockReserved) {
            order.setStatus(OrderStatus.REJECTED);
            cacheService.eliminar(redisKey);
        }

        return stockReserved;
    }

    private Order findOrder(Long id) {
        return orderRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }
}
