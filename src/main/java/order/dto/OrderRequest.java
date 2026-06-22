package order.dto;

import java.util.List;

public record OrderRequest(
        Long customerId,
        List<Products> products) {
    public record Products(
            Long productId,
            Integer quantity) {
    }
}
