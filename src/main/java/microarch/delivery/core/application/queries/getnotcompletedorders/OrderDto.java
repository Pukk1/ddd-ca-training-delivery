package microarch.delivery.core.application.queries.getnotcompletedorders;

import microarch.delivery.core.application.queries.common.LocationDto;

import java.util.UUID;

public record OrderDto(UUID id, LocationDto location) {
}
