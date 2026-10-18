package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransactionIntegrationTest extends BasePostgresContainerTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CourierRepository courierRepository;

    @Autowired
    OrderJpaRepository orderJpa;

    @Autowired
    CourierJpaRepository courierJpa;

    @Autowired
    TransactionTemplate transactionTemplate;

    @BeforeEach
    void cleanUp() {
        orderJpa.deleteAll();
        courierJpa.deleteAll();
    }

    @Test
    void assignOrder_whenTransactionCommitted_shouldPersistBothAggregates() {
        var order = createOrder();
        var courier = createCourier();
        orderRepository.add(order);
        courierRepository.add(courier);

        transactionTemplate.executeWithoutResult(status -> {
            order.assign();
            courier.assignNewOrder(order);
            orderRepository.update(order);
            courierRepository.update(courier);
        });

        assertThat(orderRepository.getById(order.getId())).get().usingRecursiveComparison().isEqualTo(order);
        assertThat(courierRepository.getById(courier.getId())).get().usingRecursiveComparison().isEqualTo(courier);
    }

    @Test
    void assignOrder_whenTransactionFailed_shouldPersistNeitherAggregate() {
        var order = createOrder();
        var courier = createCourier();

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            orderRepository.add(order);
            courierRepository.add(courier);
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(orderRepository.getById(order.getId())).isEmpty();
        assertThat(courierRepository.getById(courier.getId())).isEmpty();
    }

    private Courier createCourier() {
        return Courier.create("Иван", Location.create(1, 1).getValue()).getValue();
    }

    private Order createOrder() {
        return Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();
    }
}
