package microarch.delivery.core.domain.model.kernel;

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
import java.util.stream.Stream;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location extends ValueObject<Location> {
    private int x;
    private int y;

    private static final int MIN_X = 1;
    private static final int MAX_X = 10;
    private static final int MIN_Y = 1;
    private static final int MAX_Y = 10;

    public static Result<Location, Error> create(int x, int y) {
        var minMaxError = Stream.of(
                        Guard.againstLessThan(x, MIN_X, "x"),
                        Guard.againstGreaterThan(x, MAX_X, "x"),
                        Guard.againstLessThan(y, MIN_Y, "y"),
                        Guard.againstGreaterThan(y, MAX_Y, "y")
                )
                .filter(Objects::nonNull)
                .findFirst();
        if (minMaxError.isPresent()) {
            return Result.failure(minMaxError.get());
        }

        return Result.success(new Location(x, y));
    }

    public int distanceTo(Location other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
