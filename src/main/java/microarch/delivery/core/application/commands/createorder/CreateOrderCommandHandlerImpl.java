package microarch.delivery.core.application.commands.createorder;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {
    private final OrderRepository orderRepository;
    private final GeoClient geoClient;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository, GeoClient geoClient) {
        this.orderRepository = orderRepository;
        this.geoClient = geoClient;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateOrderCommand command) {
        var volume = Volume.create(command.getVolume());
        if (volume.isFailure()) {
            return UnitResult.failure(volume.getError());
        }

        var location = geoClient.getLocation(command.getStreet());
        if (location.isFailure()) {
            return UnitResult.failure(location.getError());
        }

        var order = Order.create(command.getOrderId(), location.getValue(), volume.getValue());
        if (order.isFailure()) {
            return UnitResult.failure(order.getError());
        }

        orderRepository.add(order.getValue());
        return UnitResult.success();
    }
}
