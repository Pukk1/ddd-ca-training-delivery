package microarch.delivery.core.domain.model.courier;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Measure;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.assignment.Assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Courier extends Aggregate<UUID> {
    private String name;
    private Location location;
    private Volume maxVolume;
    private List<Assignment> assignments;

    private static final int MAX_MOVE_DISTANCE = 1;
    private static final int MAX_VOLUME = 20;

    private Courier(UUID courierId, String name, Location location, Volume maxVolume) {
        super(courierId);
        this.name = name;
        this.location = location;
        this.assignments = new ArrayList<>();
        this.maxVolume = maxVolume;
    }

    public static Result<Courier, Error> create(String name, Location location) {
        if (Objects.isNull(name))
            return Result.failure(GeneralErrors.valueIsRequired("name"));
        if (Objects.isNull(location))
            return Result.failure(GeneralErrors.valueIsRequired("location"));

        var maxVolumeOrError = Volume.create(MAX_VOLUME, Measure.LITER);
        if (maxVolumeOrError.isFailure()) {
            return Result.failure(maxVolumeOrError.getError());
        }

        return Result.success(new Courier(UUID.randomUUID(), name, location, maxVolumeOrError.getValue()));
    }

    public Result<Boolean, Error> mayAssignNewOrder(Order order) {
        if (Objects.isNull(order))
            return Result.failure(GeneralErrors.valueIsRequired("order"));

        var currentVolume = this.assignments.stream().map(it -> it.getVolume().getVolume()).reduce(0, Integer::sum);
        if (currentVolume + order.getVolume().getVolume() > this.maxVolume.getVolume()) {
            return Result.success(false);
        } else {
            return Result.success(true);
        }
    }

    public UnitResult<Error> assignNewOrder(Order order) {
        var mayGetNewOrder = mayAssignNewOrder(order);
        if (mayGetNewOrder.isFailure()) {
            return UnitResult.failure(mayGetNewOrder.getError());
        } else {
            if (!mayGetNewOrder.getValue()) {
                return UnitResult.failure(Errors.exceededOrdersVolume());
            }
        }
        var newAssignment = Assignment.create(order.getId(), order.getVolume(), order.getLocation());
        if (newAssignment.isFailure()) {
            return UnitResult.failure(newAssignment.getError());
        }
        this.assignments.addLast(newAssignment.getValue());
        return UnitResult.success();
    }

    public UnitResult<Error> completeOrder(Order order) {
        if (Objects.isNull(order))
            return UnitResult.failure(GeneralErrors.valueIsRequired("order"));

        var orderAssignmentOptional = this.assignments.stream().filter(it -> it.getOrderId().equals(order.getId()))
                .findFirst();
        if (orderAssignmentOptional.isEmpty()) {
            return UnitResult.failure(Errors.orderAssignmentNotFound());
        }
        var orderAssignment = orderAssignmentOptional.get();
        var completeResult = orderAssignment.complete(this.location);
        if (completeResult.isFailure()) {
            return UnitResult.failure(completeResult.getError());
        } else {
            return UnitResult.success();
        }
    }

    public UnitResult<Error> moveTo(Location location) {
        if (Objects.isNull(location))
            return UnitResult.failure(GeneralErrors.valueIsRequired("location"));

        if (this.location.distanceTo(location) > MAX_MOVE_DISTANCE) {
            return UnitResult.failure(Errors.onlyOneStepMove());
        } else {
            this.location = location;
            return UnitResult.success();
        }
    }

    public static class Errors {
        public static Error exceededOrdersVolume() {
            return Error.of(Courier.class.getSimpleName().toLowerCase() + ".exceeded.orders.volume",
                    "Превышен объём заказом");
        }

        public static Error orderAssignmentNotFound() {
            return Error.of(Courier.class.getSimpleName().toLowerCase() + ".order.assignment.not.found",
                    "Назначение заказа не найдено");
        }

        public static Error onlyOneStepMove() {
            return Error.of(Courier.class.getSimpleName().toLowerCase() + ".only.one.step.move",
                    "Можно перемещаться только на один шаг");
        }
    }
}
