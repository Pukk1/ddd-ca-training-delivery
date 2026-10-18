package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.order.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    void add(Order order);

    void update(Order order);

    Optional<Order> getById(UUID orderId);

    Optional<Order> getAnyCreated();

    List<Order> getAllAssigned();
}
