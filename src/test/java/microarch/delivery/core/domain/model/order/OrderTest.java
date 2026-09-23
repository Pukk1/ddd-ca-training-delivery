package microarch.delivery.core.domain.model.order;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Measure;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void create_whenValidParameters_shouldSucceed() {
        var orderId = UUID.randomUUID();
        var location = Location.create(5, 5).getValue();
        var volume = Volume.create(3, Measure.LITER).getValue();

        var result = Order.create(orderId, location, volume);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getId()).isEqualTo(orderId);
        assertThat(result.getValue().getLocation()).isEqualTo(location);
        assertThat(result.getValue().getVolume()).isEqualTo(volume);
        assertThat(result.getValue().getStatus()).isEqualTo(Status.CREATED);
    }

    @ParameterizedTest
    @MethodSource("nullParameters")
    void create_whenNullParameter_shouldFail(UUID orderId, Location location, Volume volume) {
        var result = Order.create(orderId, location, volume);

        assertThat(result.isFailure()).isTrue();
    }

    static List<Arguments> nullParameters() {
        var orderId = UUID.randomUUID();
        var location = Location.create(5, 5).getValue();
        var volume = Volume.create(3, Measure.LITER).getValue();

        return List.of(Arguments.of(null, location, volume), Arguments.of(orderId, null, volume),
                Arguments.of(orderId, location, null));
    }

    @Test
    void assign_whenStatusCreated_shouldSucceed() {
        var order = createOrder();

        var result = order.assign();

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.ASSIGNED);
    }

    @Test
    void assign_whenStatusAssigned_shouldFail() {
        var order = createOrder();
        order.assign();

        var result = order.assign();

        assertThat(result.isFailure()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.ASSIGNED);
    }

    @Test
    void assign_whenStatusCompleted_shouldFail() {
        var order = createOrder();
        order.assign();
        order.complete();

        var result = order.assign();

        assertThat(result.isFailure()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.COMPLETED);
    }

    @Test
    void complete_whenStatusAssigned_shouldSucceed() {
        var order = createOrder();
        order.assign();

        var result = order.complete();

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.COMPLETED);
    }

    @Test
    void complete_whenStatusCreated_shouldFail() {
        var order = createOrder();

        var result = order.complete();

        assertThat(result.isFailure()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.CREATED);
    }

    @Test
    void complete_whenStatusCompleted_shouldFail() {
        var order = createOrder();
        order.assign();
        order.complete();

        var result = order.complete();

        assertThat(result.isFailure()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.COMPLETED);
    }

    private Order createOrder() {
        return Order
                .create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(3, Measure.LITER).getValue())
                .getValue();
    }
}
