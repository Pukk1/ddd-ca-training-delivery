package microarch.delivery.core.application.commands.createorder;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {
    private final OrderRepository orderRepository;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateOrderCommand command) {
        var volume = Volume.create(command.getVolume());
        if (volume.isFailure()) {
            return UnitResult.failure(volume.getError());
        }

        // Пока нет интеграции с Geo, адрес не переводится в координаты — используется случайная Location
        var order = Order.create(command.getOrderId(), Location.createRandom(), volume.getValue());
        if (order.isFailure()) {
            return UnitResult.failure(order.getError());
        }

        orderRepository.add(order.getValue());
        return UnitResult.success();
    }
}
