package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Status;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderRepositoryIntegrationTest extends BasePostgresContainerTest {

    @Autowired
    OrderRepository repository;

    @Autowired
    OrderJpaRepository jpa;

    @BeforeEach
    void cleanUp() {
        jpa.deleteAll();
    }

    @Test
    void add_whenNewOrder_shouldBeFoundById() {
        var order = createOrder();

        repository.add(order);
        var loaded = repository.getById(order.getId());

        assertThat(loaded).isPresent();
        assertThat(loaded.get()).usingRecursiveComparison().isEqualTo(order);
    }

    @Test
    void update_whenOrderAssigned_shouldPersistNewStatus() {
        var order = createOrder();
        repository.add(order);

        order.assign();
        repository.update(order);
        var loaded = repository.getById(order.getId());

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getStatus()).isEqualTo(Status.ASSIGNED);
    }

    @Test
    void getAnyCreated_whenCreatedAndAssignedOrdersExist_shouldReturnCreatedOrder() {
        var createdOrder = createOrder();
        var assignedOrder = createOrder();
        assignedOrder.assign();
        repository.add(createdOrder);
        repository.add(assignedOrder);

        var loaded = repository.getAnyCreated();

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getId()).isEqualTo(createdOrder.getId());
    }

    @Test
    void getAllAssigned_whenOrdersInDifferentStatuses_shouldReturnOnlyAssigned() {
        var createdOrder = createOrder();
        var firstAssignedOrder = createOrder();
        firstAssignedOrder.assign();
        var secondAssignedOrder = createOrder();
        secondAssignedOrder.assign();
        repository.add(createdOrder);
        repository.add(firstAssignedOrder);
        repository.add(secondAssignedOrder);

        var loaded = repository.getAllAssigned();

        assertThat(loaded).extracting(Order::getId).containsExactlyInAnyOrder(firstAssignedOrder.getId(),
                secondAssignedOrder.getId());
    }

    private Order createOrder() {
        return Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();
    }
}
