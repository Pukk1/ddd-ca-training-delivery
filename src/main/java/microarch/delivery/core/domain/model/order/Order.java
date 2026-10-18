package microarch.delivery.core.domain.model.order;

import libs.ddd.Aggregate;
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
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Order extends Aggregate<UUID> {
    @Embedded
    @AttributeOverrides({ @AttributeOverride(name = "x", column = @Column(name = "location_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "location_y", nullable = false)) })
    private Location location;

    @Embedded
    @AttributeOverride(name = "volume", column = @Column(name = "volume", nullable = false))
    private Volume volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    private Order(UUID orderId, Location location, Volume volume, Status status) {
        super(orderId);
        this.location = location;
        this.volume = volume;
        this.status = status;
    }

    public static Result<Order, Error> create(UUID orderId, Location location, Volume volume) {

        if (Objects.isNull(orderId))
            return Result.failure(GeneralErrors.valueIsRequired("order id"));
        if (Objects.isNull(location))
            return Result.failure(GeneralErrors.valueIsRequired("location"));
        if (Objects.isNull(volume)) {
            return Result.failure(GeneralErrors.valueIsRequired("volume"));
        }

        return Result.success(new Order(orderId, location, volume, Status.CREATED));
    }

    public UnitResult<Error> assign() {
        if (this.status != Status.CREATED) {
            return UnitResult.failure(Errors.orderShouldHaveCreatedStatus());
        } else {
            this.status = Status.ASSIGNED;
            return UnitResult.success();
        }
    }

    public UnitResult<Error> complete() {
        if (this.status != Status.ASSIGNED) {
            return UnitResult.failure(Errors.orderShouldHaveAssignedStatus());
        } else {
            this.status = Status.COMPLETED;
            return UnitResult.success();
        }
    }

    public static class Errors {
        public static Error orderShouldHaveCreatedStatus() {
            return Error.of(Order.class.getSimpleName().toLowerCase() + ".should.have.created.status",
                    "Заказ должен быть в статусе CREATED");
        }

        public static Error orderShouldHaveAssignedStatus() {
            return Error.of(Order.class.getSimpleName().toLowerCase() + ".should.have.assigned.status",
                    "Заказ должен быть в статусе ASSIGNED");
        }
    }
}
