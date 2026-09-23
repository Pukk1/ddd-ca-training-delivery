package microarch.delivery.core.domain.model.kernel.volume;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VolumeTest {

    @ParameterizedTest
    @MethodSource("validVolumes")
    void create_whenValidVolume_shouldSucceed(int volume, Measure measure) {
        var result = Volume.create(volume, measure);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getVolume()).isEqualTo(volume);
        assertThat(result.getValue().getMeasure()).isEqualTo(measure);
    }

    static List<Arguments> validVolumes() {
        return List.of(Arguments.of(1, Measure.LITER), Arguments.of(5, Measure.LITER),
                Arguments.of(100, Measure.LITER));
    }

    @ParameterizedTest
    @MethodSource("invalidVolumes")
    void create_whenInvalidVolume_shouldFail(int volume, Measure measure) {
        var result = Volume.create(volume, measure);

        assertThat(result.isFailure()).isTrue();
    }

    static List<Arguments> invalidVolumes() {
        return List.of(Arguments.of(0, Measure.LITER), Arguments.of(-1, Measure.LITER),
                Arguments.of(-100, Measure.LITER));
    }

    @ParameterizedTest
    @MethodSource("equalVolumes")
    void equals_whenSameComponents_shouldBeEqual(int volume, Measure measure) {
        var vol1 = Volume.create(volume, measure).getValue();
        var vol2 = Volume.create(volume, measure).getValue();

        assertThat(vol1).isEqualTo(vol2);
        assertThat(vol1.hashCode()).isEqualTo(vol2.hashCode());
    }

    static List<Arguments> equalVolumes() {
        return List.of(Arguments.of(1, Measure.LITER), Arguments.of(5, Measure.LITER),
                Arguments.of(100, Measure.LITER));
    }

    @ParameterizedTest
    @MethodSource("notEqualVolumes")
    void equals_whenDifferentComponents_shouldNotBeEqual(int volume1, Measure measure1, int volume2, Measure measure2) {
        var vol1 = Volume.create(volume1, measure1).getValue();
        var vol2 = Volume.create(volume2, measure2).getValue();

        assertThat(vol1).isNotEqualTo(vol2);
    }

    static List<Arguments> notEqualVolumes() {
        return List.of(Arguments.of(1, Measure.LITER, 5, Measure.LITER),
                Arguments.of(5, Measure.LITER, 100, Measure.LITER), Arguments.of(1, Measure.LITER, 100, Measure.LITER));
    }
}
