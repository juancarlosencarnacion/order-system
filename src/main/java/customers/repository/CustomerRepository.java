package customers.repository;

import java.util.Optional;

import customers.entity.Customer;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {
    Optional<Customer> findByEmail(String email) {
        return null;
    }
}
