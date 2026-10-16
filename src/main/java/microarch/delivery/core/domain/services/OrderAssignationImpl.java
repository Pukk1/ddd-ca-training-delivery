package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Status;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.List;

public class OrderAssignationImpl implements OrderAssignation {

    @Override
    public Result<Courier, Error> assignOrder(Order order, List<Courier> couriers) {

        if (order == null) {
            return Result.failure(GeneralErrors.valueIsRequired("order"));
        }
        var status = order.getStatus();
        if (status != Status.CREATED) {
            return Result.failure(GeneralErrors.valueIsInvalid("order status is illegal", status));
        }

        var mayAssignCouriers = new ArrayList<Courier>();
        for (Courier courier : couriers) {
            var mayAssign = courier.mayAssignNewOrder(order);
            if (mayAssign.isSuccess() && mayAssign.getValue()) {
                mayAssignCouriers.add(courier);
            }
        }
        Pair<Integer, Courier> minimalDistanceCourier = null;
        for (Courier mayAssignCourier : mayAssignCouriers) {
            var currentDistanceTo = mayAssignCourier.getLocation().distanceTo(order.getLocation());
            if (minimalDistanceCourier == null || minimalDistanceCourier.a < currentDistanceTo) {
                minimalDistanceCourier = new Pair<>(currentDistanceTo, mayAssignCourier);
            }
        }
        if (minimalDistanceCourier == null) {
            return Result
                    .failure(Error.of(OrderAssignationImpl.class.getSimpleName().toLowerCase(), "courier not found"));
        } else {
            return Result.success(minimalDistanceCourier.b);
        }
    }
}
