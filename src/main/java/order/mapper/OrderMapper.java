package order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import order.dto.OrderResponse;
import order.entity.Order;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerEmail", source = "customer.email")

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productPrice", source = "product.price")
    OrderResponse toResponse(Order order);
}
