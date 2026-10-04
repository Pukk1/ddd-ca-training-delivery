package microarch.delivery.core.domain.model.kernel.volume;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VolumeTest {

    @ParameterizedTest
    @MethodSource("validVolumes")
    void create_whenValidVolume_shouldSucceed(int volume) {
        var result = Volume.create(volume);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getVolume()).isEqualTo(volume);
    }

    static List<Arguments> validVolumes() {
        return List.of(Arguments.of(1), Arguments.of(5), Arguments.of(100));
    }

    @ParameterizedTest
    @MethodSource("invalidVolumes")
    void create_whenInvalidVolume_shouldFail(int volume) {
        var result = Volume.create(volume);

        assertThat(result.isFailure()).isTrue();
    }

    static List<Arguments> invalidVolumes() {
        return List.of(Arguments.of(-1), Arguments.of(-100));
    }

    @ParameterizedTest
    @MethodSource("equalVolumes")
    void equals_whenSameComponents_shouldBeEqual(int volume) {
        var vol1 = Volume.create(volume).getValue();
        var vol2 = Volume.create(volume).getValue();

        assertThat(vol1).isEqualTo(vol2);
        assertThat(vol1.hashCode()).isEqualTo(vol2.hashCode());
    }

    static List<Arguments> equalVolumes() {
        return List.of(Arguments.of(1), Arguments.of(5), Arguments.of(100));
    }

    @ParameterizedTest
    @MethodSource("notEqualVolumes")
    void equals_whenDifferentComponents_shouldNotBeEqual(int volume1, int volume2) {
        var vol1 = Volume.create(volume1).getValue();
        var vol2 = Volume.create(volume2).getValue();

        assertThat(vol1).isNotEqualTo(vol2);
    }

    static List<Arguments> notEqualVolumes() {
        return List.of(Arguments.of(1, 5), Arguments.of(5, 100), Arguments.of(1, 100));
    }
}
