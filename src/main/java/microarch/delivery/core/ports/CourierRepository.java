package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.courier.Courier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourierRepository {
    void add(Courier courier);

    void update(Courier courier);

    Optional<Courier> getById(UUID courierId);

    List<Courier> getAll();
}
