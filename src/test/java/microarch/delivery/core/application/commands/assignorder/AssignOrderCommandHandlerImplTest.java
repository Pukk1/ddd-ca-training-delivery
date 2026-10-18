package microarch.delivery.core.application.commands.assignorder;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Status;
import microarch.delivery.core.domain.services.OrderAssignationImpl;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignOrderCommandHandlerImplTest {

    private OrderRepository orderRepository;
    private CourierRepository courierRepository;
    private AssignOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        courierRepository = mock(CourierRepository.class);
        handler = new AssignOrderCommandHandlerImpl(orderRepository, courierRepository, new OrderAssignationImpl());
    }

    @Test
    void handle_whenCreatedOrderAndCouriersExist_shouldAssignOrderToNearestCourier() {
        var order = createOrder();
        var farCourier = createCourier(10, 10);
        var nearCourier = createCourier(2, 2);
        when(orderRepository.getAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.getAll()).thenReturn(List.of(farCourier, nearCourier));

        var result = handler.handle(AssignOrderCommand.create().getValue());

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.ASSIGNED);
        assertThat(nearCourier.getAssignments()).hasSize(1);
        assertThat(farCourier.getAssignments()).isEmpty();
        verify(courierRepository).update(nearCourier);
        verify(orderRepository).update(order);
    }

    @Test
    void handle_whenNoCreatedOrder_shouldSucceedWithoutChanges() {
        when(orderRepository.getAnyCreated()).thenReturn(Optional.empty());

        var result = handler.handle(AssignOrderCommand.create().getValue());

        assertThat(result.isSuccess()).isTrue();
        verify(courierRepository, never()).update(any());
        verify(orderRepository, never()).update(any());
    }

    @Test
    void handle_whenNoCourierAvailable_shouldFailWithoutChanges() {
        var order = createOrder();
        when(orderRepository.getAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.getAll()).thenReturn(List.of());

        var result = handler.handle(AssignOrderCommand.create().getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Status.CREATED);
        verify(courierRepository, never()).update(any());
        verify(orderRepository, never()).update(any());
    }

    private Order createOrder() {
        return Order.create(UUID.randomUUID(), Location.create(1, 1).getValue(), Volume.create(5).getValue())
                .getValue();
    }

    private Courier createCourier(int x, int y) {
        return Courier.create("Иван", Location.create(x, y).getValue()).getValue();
    }
}
