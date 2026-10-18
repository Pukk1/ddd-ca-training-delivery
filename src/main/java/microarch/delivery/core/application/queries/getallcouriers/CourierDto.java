package microarch.delivery.core.application.queries.getallcouriers;

import microarch.delivery.core.application.queries.common.LocationDto;

import java.util.UUID;

public record CourierDto(UUID id, String name, LocationDto location) {
}
