package microarch.delivery.core.domain.model.courier;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Measure;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourierTest {

    @Test
    void create_whenValidParameters_shouldSucceed() {
        var name = "Courier 1";
        var location = Location.create(5, 5).getValue();

        var result = Courier.create(name, location);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getName()).isEqualTo(name);
        assertThat(result.getValue().getLocation()).isEqualTo(location);
        assertThat(result.getValue().getAssignments()).isEmpty();
    }

    @Test
    void create_whenNullName_shouldFail() {
        var location = Location.create(5, 5).getValue();

        var result = Courier.create(null, location);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_whenNullLocation_shouldFail() {
        var result = Courier.create("Courier 1", null);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void mayAssignNewOrder_whenVolumeWithinLimit_shouldReturnTrue() {
        var courier = createCourier();
        var order = createOrder(5);

        var result = courier.mayAssignNewOrder(order);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isTrue();
    }

    @Test
    void mayAssignNewOrder_whenVolumeExceedsLimit_shouldReturnFalse() {
        var courier = createCourier();
        var order = createOrder(21);

        var result = courier.mayAssignNewOrder(order);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isFalse();
    }

    @Test
    void mayAssignNewOrder_whenNullOrder_shouldFail() {
        var courier = createCourier();

        var result = courier.mayAssignNewOrder(null);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void assignNewOrder_whenVolumeWithinLimit_shouldSucceed() {
        var courier = createCourier();
        var order = createOrder(5);

        var result = courier.assignNewOrder(order);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void assignNewOrder_whenMultipleOrdersWithinLimit_shouldSucceed() {
        var courier = createCourier();
        var order1 = createOrder(5);
        var order2 = createOrder(10);

        courier.assignNewOrder(order1);
        var result = courier.assignNewOrder(order2);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments()).hasSize(2);
    }

    @Test
    void assignNewOrder_whenVolumeExceedsLimit_shouldFail() {
        var courier = createCourier();
        var order = createOrder(21);

        var result = courier.assignNewOrder(order);

        assertThat(result.isFailure()).isTrue();
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void assignNewOrder_whenCumulativeVolumeExceedsLimit_shouldFail() {
        var courier = createCourier();
        var order1 = createOrder(15);
        var order2 = createOrder(10);

        courier.assignNewOrder(order1);
        var result = courier.assignNewOrder(order2);

        assertThat(result.isFailure()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void completeOrder_whenOrderAssigned_shouldSucceed() {
        var courier = createCourierAt(5, 5);
        var order = createOrderAt(5, 5, 3);
        courier.assignNewOrder(order);

        var result = courier.completeOrder(order);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void completeOrder_whenOrderNotAssigned_shouldFail() {
        var courier = createCourier();
        var order = createOrder(3);

        var result = courier.completeOrder(order);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void completeOrder_whenNullOrder_shouldFail() {
        var courier = createCourier();

        var result = courier.completeOrder(null);

        assertThat(result.isFailure()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("validMoveLocations")
    void moveTo_whenOneStep_shouldSucceed(int targetX, int targetY) {
        var courier = createCourierAt(5, 5);

        var target = Location.create(targetX, targetY).getValue();
        var result = courier.moveTo(target);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(target);
    }

    static List<Arguments> validMoveLocations() {
        return List.of(Arguments.of(5, 5), Arguments.of(4, 5), Arguments.of(6, 5), Arguments.of(5, 4),
                Arguments.of(5, 6));
    }

    @ParameterizedTest
    @MethodSource("tooFarMoveLocations")
    void moveTo_whenMoreThanOneStep_shouldFail(int targetX, int targetY) {
        var courier = createCourierAt(5, 5);

        var target = Location.create(targetX, targetY).getValue();
        var result = courier.moveTo(target);

        assertThat(result.isFailure()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(Location.create(5, 5).getValue());
    }

    static List<Arguments> tooFarMoveLocations() {
        return List.of(Arguments.of(3, 5), Arguments.of(7, 5), Arguments.of(5, 3), Arguments.of(5, 7),
                Arguments.of(1, 1));
    }

    @Test
    void moveTo_whenNullLocation_shouldFail() {
        var courier = createCourier();

        var result = courier.moveTo(null);

        assertThat(result.isFailure()).isTrue();
    }

    private Courier createCourier() {
        return Courier.create("Courier 1", Location.create(5, 5).getValue()).getValue();
    }

    private Courier createCourierAt(int x, int y) {
        return Courier.create("Courier 1", Location.create(x, y).getValue()).getValue();
    }

    private Order createOrder(int volume) {
        return Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(),
                Volume.create(volume, Measure.LITER).getValue()).getValue();
    }

    private Order createOrderAt(int x, int y, int volume) {
        return Order.create(UUID.randomUUID(), Location.create(x, y).getValue(),
                Volume.create(volume, Measure.LITER).getValue()).getValue();
    }
}
