package microarch.delivery.adapters.in.http.mapper;

import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.core.application.queries.getallcouriers.CourierDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = LocationDtoMapper.class)
public interface CourierDtoMapper {
    List<Courier> fromDtos(List<CourierDto> dtos);
}
