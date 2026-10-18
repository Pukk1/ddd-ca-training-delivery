package microarch.delivery.adapters.in.http.mapper;

import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.queries.common.LocationDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationDtoMapper {
    Location fromDto(LocationDto dto);
}
