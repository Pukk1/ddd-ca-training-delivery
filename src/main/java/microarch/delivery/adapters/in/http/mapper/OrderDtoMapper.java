package microarch.delivery.adapters.in.http.mapper;

import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.queries.getnotcompletedorders.OrderDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = LocationDtoMapper.class)
public interface OrderDtoMapper {
    List<Order> fromDtos(List<OrderDto> dtos);
}
