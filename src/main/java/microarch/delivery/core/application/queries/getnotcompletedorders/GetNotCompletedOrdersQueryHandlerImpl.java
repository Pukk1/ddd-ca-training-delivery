package microarch.delivery.core.application.queries.getnotcompletedorders;

import microarch.delivery.core.application.queries.common.LocationDto;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetNotCompletedOrdersQueryHandlerImpl implements GetNotCompletedOrdersQueryHandler {
    private final OrderRepository orderRepository;

    public GetNotCompletedOrdersQueryHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> handle(GetNotCompletedOrdersQuery query) {
        return orderRepository.getAllNotCompleted().stream()
                .map(order -> new OrderDto(order.getId(), LocationDto.from(order.getLocation()))).toList();
    }
}
