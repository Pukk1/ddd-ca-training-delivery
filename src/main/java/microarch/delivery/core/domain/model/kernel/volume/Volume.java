package microarch.delivery.core.domain.model.kernel.volume;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Volume extends ValueObject<Volume> {
    private int volume;

    private static final int MIN_VOLUME = 0;

    public static Result<Volume, Error> create(int volume) {
        var minVolumeError = Guard.againstLessThan(volume, MIN_VOLUME, "volume");
        if (Objects.nonNull(minVolumeError)) {
            return Result.failure(minVolumeError);
        }

        return Result.success(new Volume(volume));
    }

    public static Volume emptyVolume() {
        return new Volume(0);
    }

    public Volume add(Volume otherVolume) {
        return new Volume(this.volume + otherVolume.getVolume());
    }

    public boolean isGreaterThen(Volume otherVolume) {
        return this.volume > otherVolume.getVolume();
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(volume);
    }
}
