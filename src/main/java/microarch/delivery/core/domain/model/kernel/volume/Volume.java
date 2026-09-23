package microarch.delivery.core.domain.model.kernel.volume;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Volume extends ValueObject<Volume> {
    private int volume;
    private Measure measure;

    private static final int MIN_VOLUME = 1;

    public static Result<Volume, Error> create(int volume, Measure measure) {
        var minVolumeError = Guard.againstLessThan(volume, MIN_VOLUME, "volume");
        if (Objects.nonNull(minVolumeError)) {
            return Result.failure(minVolumeError);
        }

        return Result.success(new Volume(volume, measure));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(volume, measure);
    }
}
