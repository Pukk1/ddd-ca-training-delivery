package microarch.delivery.core.application.commands.completeorder;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var courierOptional = courierRepository.getById(command.getCourierId());
        if (courierOptional.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        }
        var courier = courierOptional.get();

        var orderOptional = orderRepository.getById(command.getOrderId());
        if (orderOptional.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("order", command.getOrderId()));
        }
        var order = orderOptional.get();

        var courierCompleteResult = courier.completeOrder(order);
        if (courierCompleteResult.isFailure()) {
            return courierCompleteResult;
        }
        var orderCompleteResult = order.complete();
        if (orderCompleteResult.isFailure()) {
            return orderCompleteResult;
        }

        courierRepository.update(courier);
        orderRepository.update(order);
        return UnitResult.success();
    }
}
