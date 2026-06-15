package customers.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import customers.dto.CustomerRequest;
import customers.dto.CustomerResponse;
import customers.entity.Customer;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerRequest request);
}
