package customers.service;

import java.util.List;

import customers.dto.CustomerRequest;
import customers.dto.CustomerResponse;
import customers.entity.Customer;
import customers.mapper.CustomerMapper;
import customers.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import redis.service.CacheService;

@ApplicationScoped
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    // REDIS
    private final CacheService cacheService;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Saving in DB
        Customer newCustomer = customerMapper.toEntity(request);
        customerRepository.persist(newCustomer);

        // Preparing response
        CustomerResponse response = customerMapper.toResponse(newCustomer);

        // Saving in REDIS
        String redisKey = "customer:" + newCustomer.getId();
        cacheService.guardarJson(redisKey, response);

        return response;
    }

    public List<CustomerResponse> getCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    public CustomerResponse getCustomerById(Long id) {

        // Searching in redis (Cache hit)
        String redisKey = "customer:" + id;
        CustomerResponse cachedCustomer = cacheService.obtenerJson(redisKey, CustomerResponse.class);

        if (cachedCustomer != null) {
            return cachedCustomer;
        }

        // Searching in DB
        Customer customer = customerRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        CustomerResponse response = customerMapper.toResponse(customer);

        // Caching in REDIS
        cacheService.guardarJson(redisKey, response);

        return response;
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.deleteById(id)) {
            throw new NotFoundException("Customer not found and could not be deleted");
        }
        
        // 2. Desalojo del caché (Evita datos fantasma en Redis)
        String redisKey = "customer:" + id;
        cacheService.eliminar(redisKey);
    }
}
