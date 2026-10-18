package microarch.delivery.core.domain.services;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderAssignationImplTest {

    private final OrderAssignation orderAssignation = new OrderAssignationImpl();

    @Test
    void assignOrder_whenSeveralCouriers_shouldReturnNearest() {
        var order = createOrder(3);
        var farCourier = createCourier(10, 10);
        var nearCourier = createCourier(6, 6);

        var result = orderAssignation.assignOrder(order, List.of(farCourier, nearCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(nearCourier);
    }

    @Test
    void assignOrder_whenNearestCourierIsFull_shouldReturnCourierWithFreeVolume() {
        var order = createOrder(3);
        var fullCourier = createCourier(5, 5);
        fullCourier.assignNewOrder(createOrder(20));
        var freeCourier = createCourier(10, 10);

        var result = orderAssignation.assignOrder(order, List.of(fullCourier, freeCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(freeCourier);
    }

    @Test
    void assignOrder_whenNoCourierCanTakeOrder_shouldFail() {
        var fullCourier = createCourier(5, 5);
        fullCourier.assignNewOrder(createOrder(20));

        var result = orderAssignation.assignOrder(createOrder(3), List.of(fullCourier));

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void assignOrder_whenOrderIsNull_shouldFail() {
        var result = orderAssignation.assignOrder(null, List.of(createCourier(5, 5)));

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void assignOrder_whenOrderNotCreated_shouldFail() {
        var order = createOrder(3);
        order.assign();

        var result = orderAssignation.assignOrder(order, List.of(createCourier(5, 5)));

        assertThat(result.isFailure()).isTrue();
    }

    private Order createOrder(int volume) {
        return Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(volume).getValue())
                .getValue();
    }

    private Courier createCourier(int x, int y) {
        return Courier.create("courier", Location.create(x, y).getValue()).getValue();
    }
}
