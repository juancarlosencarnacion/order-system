package order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import order.dto.OrderResponse;
import order.entity.Order;
import order.entity.OrderDetails;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "details", source = "details")
    OrderResponse toResponse(Order order);

    @Mapping(target = "orderDetailId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "category", source = "product.category")
    OrderResponse.OrderDetails toResponse(OrderDetails details);
}