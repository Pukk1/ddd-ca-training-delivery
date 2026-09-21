package microarch.delivery.core.domain.model.kernel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @ParameterizedTest
    @MethodSource("validCoordinates")
    void create_whenValidCoordinates_shouldSucceed(int x, int y) {
        var result = Location.create(x, y);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(x);
        assertThat(result.getValue().getY()).isEqualTo(y);
    }

    static List<Arguments> validCoordinates() {
        return List.of(
                Arguments.of(1, 1),
                Arguments.of(10, 10),
                Arguments.of(5, 5),
                Arguments.of(1, 10),
                Arguments.of(10, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCoordinates")
    void create_whenInvalidCoordinates_shouldFail(int x, int y) {
        var result = Location.create(x, y);

        assertThat(result.isFailure()).isTrue();
    }

    static List<Arguments> invalidCoordinates() {
        return List.of(
                Arguments.of(0, 5),
                Arguments.of(11, 5),
                Arguments.of(5, 0),
                Arguments.of(5, 11),
                Arguments.of(0, 0),
                Arguments.of(11, 11),
                Arguments.of(-1, 5),
                Arguments.of(5, -1)
        );
    }

    @ParameterizedTest
    @MethodSource("equalLocations")
    void equals_whenSameCoordinates_shouldBeEqual(int x, int y) {
        var loc1 = Location.create(x, y).getValue();
        var loc2 = Location.create(x, y).getValue();

        assertThat(loc1).isEqualTo(loc2);
        assertThat(loc1.hashCode()).isEqualTo(loc2.hashCode());
    }

    static List<Arguments> equalLocations() {
        return List.of(
                Arguments.of(1, 1),
                Arguments.of(5, 5),
                Arguments.of(10, 10),
                Arguments.of(1, 10),
                Arguments.of(10, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("notEqualLocations")
    void equals_whenDifferentCoordinates_shouldNotBeEqual(int x1, int y1, int x2, int y2) {
        var loc1 = Location.create(x1, y1).getValue();
        var loc2 = Location.create(x2, y2).getValue();

        assertThat(loc1).isNotEqualTo(loc2);
    }

    static List<Arguments> notEqualLocations() {
        return List.of(
                Arguments.of(1, 1, 2, 2),
                Arguments.of(1, 1, 1, 2),
                Arguments.of(1, 1, 2, 1),
                Arguments.of(5, 5, 10, 10)
        );
    }

    @ParameterizedTest
    @MethodSource("distanceCases")
    void distanceTo_shouldReturnManhattanDistance(int x1, int y1, int x2, int y2, int expected) {
        var loc1 = Location.create(x1, y1).getValue();
        var loc2 = Location.create(x2, y2).getValue();

        assertThat(loc1.distanceTo(loc2)).isEqualTo(expected);
    }

    static List<Arguments> distanceCases() {
        return List.of(
                Arguments.of(1, 1, 1, 1, 0),
                Arguments.of(1, 1, 3, 4, 5),
                Arguments.of(3, 4, 1, 1, 5),
                Arguments.of(1, 1, 10, 10, 18),
                Arguments.of(5, 5, 5, 5, 0),
                Arguments.of(1, 5, 10, 5, 9),
                Arguments.of(5, 1, 5, 10, 9)
        );
    }
}
