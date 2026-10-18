package microarch.delivery.core.domain.model.order.assignment;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;

import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "assignments")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assignment extends BaseEntity<UUID> {
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Embedded
    @AttributeOverride(name = "volume", column = @Column(name = "volume", nullable = false))
    private Volume volume;

    @Embedded
    @AttributeOverrides({ @AttributeOverride(name = "x", column = @Column(name = "location_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "location_y", nullable = false)) })
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    private static final int MAX_COMPLETABLE_DISTANCE = 1;

    private Assignment(UUID uuid, UUID orderId, Volume volume, Location location, Status status) {
        super(uuid);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = status;
    }

    public static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {

        if (Objects.isNull(orderId))
            return Result.failure(GeneralErrors.valueIsRequired("order id"));
        if (Objects.isNull(volume))
            return Result.failure(GeneralErrors.valueIsRequired("volume"));
        if (Objects.isNull(location))
            return Result.failure(GeneralErrors.valueIsRequired("location"));

        return Result.success(new Assignment(UUID.randomUUID(), orderId, volume, location, Status.ASSIGNED));
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
