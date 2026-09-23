package microarch.delivery.core.domain.model.order.assignment;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.order.Volume;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Assignment extends BaseEntity<UUID> {
    private UUID orderId;
    private Volume volume;
    private Location location;
    private Status status;

    private final int MAX_COMPLETABLE_DISTANCE = 1;

    public static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {

        if (Objects.isNull(orderId)) return Result.failure(GeneralErrors.valueIsRequired("order id"));
        if (Objects.isNull(volume)) return Result.failure(GeneralErrors.valueIsRequired("volume"));
        if (Objects.isNull(location)) return Result.failure(GeneralErrors.valueIsRequired("location"));

        return Result.success(new Assignment(orderId, volume, location, Status.ASSIGNED));
    }


    public UnitResult<Error> complete(Location courierLocation) {
        var distance = location.distanceTo(courierLocation);
        if (distance > MAX_COMPLETABLE_DISTANCE) {
            return UnitResult
                    .failure(GeneralErrors.valueMustBeLessOrEqual("distance", distance, MAX_COMPLETABLE_DISTANCE));
        }

        this.status = Status.COMPLETED;
        return UnitResult.success();
    }
}
