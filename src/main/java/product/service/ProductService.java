package product.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import order.dto.OrderResponse.OrderDetails;
import product.dto.ProductRequest;
import product.dto.ProductResponse;
import product.entity.Product;
import product.entity.ProductCategory;
import product.mapper.ProductMapper;
import product.repository.ProductRepository;
import redis.service.CacheService;

@ApplicationScoped
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // REDIS
    private final CacheService cacheService;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        Product newProduct = productMapper.toEntity(request);

        // Blindaje contra Strings inválidos en el Enum
        try {
            newProduct.setCategory(ProductCategory.valueOf(request.category().toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Invalid product category: " + request.category());
        }

        productRepository.persist(newProduct);
        ProductResponse response = productMapper.toResponse(newProduct);

        // Saving in REDIS
        String redisKey = "product:" + newProduct.getId();
        cacheService.guardarJson(redisKey, response);

        return response;
    }

    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {

        String redisKey = "product:" + id;
        ProductResponse productCached = cacheService.obtenerJson(redisKey, ProductResponse.class);
        if (productCached != null) {
            return productCached;
        }

        Product product = findProduct(id);

        ProductResponse response = productMapper.toResponse(product);
        cacheService.guardarJson(redisKey, response);

        return response;
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.deleteById(id)) {
            throw new NotFoundException("Product not found and could not be deleted");
        }

        // 2. Desalojo del caché (Evita datos fantasma en Redis)
        String redisKey = "product:" + id;
        cacheService.eliminar(redisKey);
    }

    @Transactional
    public boolean validateAndReserveStock(List<OrderDetails> details) {
        if (details == null || details.isEmpty()) {
            return false;
        }

        List<Long> productIds = details.stream()
                .map(OrderDetails::productId)
                .toList();

        List<Product> products = productRepository.list("id in ?1", productIds);

        Map<Long, Product> productsMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (var item : details) {
            Product product = productsMap.get(item.productId());

            if (product == null || product.getStock() - item.quantity() < 0) {
                System.out.println("Not enough stock available for product ID: " + product.getId());
                return false;
            }
        }

        for (var item : details) {
            Product product = productsMap.get(item.productId());
            product.setStock(product.getStock() - item.quantity());
        }

        for (var item : details) {
            String redisKey = "product:" + item.productId();
            cacheService.eliminar(redisKey);
        }

        return true;
    }

    private Product findProduct(Long id) {
        return productRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }
}
