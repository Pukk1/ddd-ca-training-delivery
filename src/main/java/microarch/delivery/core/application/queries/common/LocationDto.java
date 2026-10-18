package microarch.delivery.core.application.queries.common;

import microarch.delivery.core.domain.model.kernel.Location;

public record LocationDto(int x, int y) {

    public static LocationDto from(Location location) {
        return new LocationDto(location.getX(), location.getY());
    }
}
