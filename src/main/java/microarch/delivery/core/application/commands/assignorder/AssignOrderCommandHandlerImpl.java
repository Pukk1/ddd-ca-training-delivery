package microarch.delivery.core.application.commands.assignorder;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.services.OrderAssignation;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderAssignation orderAssignation;

    public AssignOrderCommandHandlerImpl(OrderRepository orderRepository, CourierRepository courierRepository,
            OrderAssignation orderAssignation) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.orderAssignation = orderAssignation;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrderCommand command) {
        var orderOptional = orderRepository.getAnyCreated();
        // Отсутствие неназначенных заказов — штатная ситуация: назначать нечего
        if (orderOptional.isEmpty()) {
            return UnitResult.success();
        }
        var order = orderOptional.get();

        var courierResult = orderAssignation.assignOrder(order, courierRepository.getAll());
        if (courierResult.isFailure()) {
            return UnitResult.failure(courierResult.getError());
        }
        var courier = courierResult.getValue();

        var courierAssignResult = courier.assignNewOrder(order);
        if (courierAssignResult.isFailure()) {
            return courierAssignResult;
        }
        var orderAssignResult = order.assign();
        if (orderAssignResult.isFailure()) {
            return orderAssignResult;
        }

        courierRepository.update(courier);
        orderRepository.update(order);
        return UnitResult.success();
    }
}
