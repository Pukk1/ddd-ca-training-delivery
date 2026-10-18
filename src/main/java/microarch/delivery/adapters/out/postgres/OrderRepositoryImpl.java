package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Status;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpa;

    public OrderRepositoryImpl(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void add(Order order) {
        jpa.save(order);
    }

    @Override
    public void update(Order order) {
        jpa.save(order);
    }

    @Override
    public Optional<Order> getById(UUID orderId) {
        return jpa.findById(orderId);
    }

    @Override
    public Optional<Order> getAnyCreated() {
        return jpa.findFirstByStatus(Status.CREATED);
    }

    @Override
    public List<Order> getAllAssigned() {
        return jpa.findAllByStatus(Status.ASSIGNED);
    }

    @Override
    public List<Order> getAllNotCompleted() {
        return jpa.findAllByStatusIn(List.of(Status.CREATED, Status.ASSIGNED));
    }
}
