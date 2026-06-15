package order.dto;

public record OrderRequest(
    Long customerId,
    Long productId,
    Integer quantity
) {

}
