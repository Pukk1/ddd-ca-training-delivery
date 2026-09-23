package microarch.delivery.core.domain.model.order.assignment;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.domain.model.order.VolumeMeasure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentTest {

    @Test
    void equals_whenSameParameters_shouldNotBeEqual() {
        var orderId = UUID.randomUUID();
        var volume = Volume.create(1, VolumeMeasure.LITER).getValue();
        var location = Location.create(5, 5).getValue();

        var assignment1 = Assignment.create(orderId, volume, location).getValue();
        var assignment2 = Assignment.create(orderId, volume, location).getValue();

        assertThat(assignment1).isNotEqualTo(assignment2);
    }

    @Test
    void create_whenValidParameters_shouldSucceed() {
        var orderId = UUID.randomUUID();
        var volume = Volume.create(1, VolumeMeasure.LITER).getValue();
        var location = Location.create(5, 5).getValue();

        var result = Assignment.create(orderId, volume, location);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(result.getValue().getVolume()).isEqualTo(volume);
        assertThat(result.getValue().getLocation()).isEqualTo(location);
        assertThat(result.getValue().getStatus()).isEqualTo(Status.ASSIGNED);
    }

    @ParameterizedTest
    @MethodSource("nullParameters")
    void create_whenNullParameter_shouldFail(UUID orderId, Volume volume, Location location) {
        var result = Assignment.create(orderId, volume, location);

        assertThat(result.isFailure()).isTrue();
    }

    static List<Arguments> nullParameters() {
        var orderId = UUID.randomUUID();
        var volume = Volume.create(1, VolumeMeasure.LITER).getValue();
        var location = Location.create(5, 5).getValue();

        return List.of(Arguments.of(null, volume, location), Arguments.of(orderId, null, location),
                Arguments.of(orderId, volume, null));
    }

    @ParameterizedTest
    @MethodSource("completableLocations")
    void complete_whenCourierCloseEnough_shouldSucceed(int courierX, int courierY) {
        var assignment = Assignment.create(UUID.randomUUID(), Volume.create(1, VolumeMeasure.LITER).getValue(),
                Location.create(5, 5).getValue()).getValue();

        var courierLocation = Location.create(courierX, courierY).getValue();
        var result = assignment.complete(courierLocation);

        assertThat(result.isSuccess()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(Status.COMPLETED);
    }

    static List<Arguments> completableLocations() {
        return List.of(Arguments.of(5, 5), Arguments.of(4, 5), Arguments.of(6, 5), Arguments.of(5, 4),
                Arguments.of(5, 6));
    }

    @ParameterizedTest
    @MethodSource("tooFarLocations")
    void complete_whenCourierTooFar_shouldFail(int courierX, int courierY) {
        var assignment = Assignment.create(UUID.randomUUID(), Volume.create(1, VolumeMeasure.LITER).getValue(),
                Location.create(5, 5).getValue()).getValue();

        var courierLocation = Location.create(courierX, courierY).getValue();
        var result = assignment.complete(courierLocation);

        assertThat(result.isFailure()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(Status.ASSIGNED);
    }

    static List<Arguments> tooFarLocations() {
        return List.of(Arguments.of(3, 5), Arguments.of(7, 5), Arguments.of(5, 3), Arguments.of(5, 7),
                Arguments.of(1, 1));
    }
}
