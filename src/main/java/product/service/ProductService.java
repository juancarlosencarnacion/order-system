package product.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
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

        Product product = productRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

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
}
