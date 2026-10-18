package microarch.delivery.core.application.queries.getnotcompletedorders;

import microarch.delivery.core.application.queries.common.LocationDto;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetNotCompletedOrdersQueryHandlerImplTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final GetNotCompletedOrdersQueryHandler handler = new GetNotCompletedOrdersQueryHandlerImpl(
            orderRepository);

    @Test
    void handle_whenNotCompletedOrdersExist_shouldReturnDtoForEachOrder() {
        var first = createOrder(1, 2);
        var second = createOrder(3, 4);
        when(orderRepository.getAllNotCompleted()).thenReturn(List.of(first, second));

        var result = handler.handle(GetNotCompletedOrdersQuery.create().getValue());

        assertThat(result).containsExactly(new OrderDto(first.getId(), new LocationDto(1, 2)),
                new OrderDto(second.getId(), new LocationDto(3, 4)));
    }

    @Test
    void handle_whenNoNotCompletedOrders_shouldReturnEmptyList() {
        when(orderRepository.getAllNotCompleted()).thenReturn(List.of());

        var result = handler.handle(GetNotCompletedOrdersQuery.create().getValue());

        assertThat(result).isEmpty();
    }

    private Order createOrder(int x, int y) {
        return Order.create(UUID.randomUUID(), Location.create(x, y).getValue(), Volume.create(5).getValue())
                .getValue();
    }
}
