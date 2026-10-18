package microarch.delivery.core.application.commands.createorder;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Status;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CreateOrderCommandHandlerImplTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CreateOrderCommandHandler handler = new CreateOrderCommandHandlerImpl(orderRepository);

    @Test
    void handle_whenCommandIsValid_shouldAddCreatedOrder() {
        var orderId = UUID.randomUUID();
        var command = CreateOrderCommand.create(orderId, "Россия", "Москва", "Тверская", "1", "10", 5).getValue();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).add(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(orderId);
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.CREATED);
        assertThat(captor.getValue().getVolume().getVolume()).isEqualTo(5);
    }

    @Test
    void create_whenVolumeIsNotPositive_shouldFail() {
        var command = CreateOrderCommand.create(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "10", 0);

        assertThat(command.isFailure()).isTrue();
    }
}
