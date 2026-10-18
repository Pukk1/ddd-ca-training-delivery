package microarch.delivery.core.application.commands.completeorder;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Status;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteOrderCommandHandlerImplTest {

    private CourierRepository courierRepository;
    private OrderRepository orderRepository;
    private CompleteOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        courierRepository = mock(CourierRepository.class);
        orderRepository = mock(OrderRepository.class);
        handler = new CompleteOrderCommandHandlerImpl(courierRepository, orderRepository);
    }

    @Test
    void handle_whenCourierIsAtOrderLocation_shouldCompleteOrder() {
        var location = Location.create(3, 3).getValue();
        var order = createAssignedOrder(location);
        var courier = Courier.create("Иван", location).getValue();
        courier.assignNewOrder(order);
        when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
        when(orderRepository.getById(order.getId())).thenReturn(Optional.of(order));

        var result = handler.handle(CompleteOrderCommand.create(courier.getId(), order.getId()).getValue());

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.COMPLETED);
        verify(courierRepository).update(courier);
        verify(orderRepository).update(order);
    }

    @Test
    void handle_whenCourierNotFound_shouldFailWithoutChanges() {
        var courierId = UUID.randomUUID();
        when(courierRepository.getById(courierId)).thenReturn(Optional.empty());

        var result = handler.handle(CompleteOrderCommand.create(courierId, UUID.randomUUID()).getValue());

        assertThat(result.isFailure()).isTrue();
        verify(courierRepository, never()).update(any());
        verify(orderRepository, never()).update(any());
    }

    private Order createAssignedOrder(Location location) {
        var order = Order.create(UUID.randomUUID(), location, Volume.create(5).getValue()).getValue();
        order.assign();
        return order;
    }
}
