package microarch.delivery.core.application.commands.createorder;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Status;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateOrderCommandHandlerImplTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final GeoClient geoClient = mock(GeoClient.class);
    private final CreateOrderCommandHandler handler = new CreateOrderCommandHandlerImpl(orderRepository, geoClient);

    @Test
    void handle_whenCommandIsValid_shouldAddCreatedOrderWithLocationFromGeo() {
        var orderId = UUID.randomUUID();
        var command = CreateOrderCommand.create(orderId, "Россия", "Москва", "Айтишная", "1", "10", 5).getValue();
        when(geoClient.getLocation("Айтишная")).thenReturn(Result.success(Location.create(2, 2).getValue()));

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).add(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(orderId);
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.CREATED);
        assertThat(captor.getValue().getVolume().getVolume()).isEqualTo(5);
        assertThat(captor.getValue().getLocation()).isEqualTo(Location.create(2, 2).getValue());
    }

    @Test
    void handle_whenGeoFails_shouldFailWithoutAddingOrder() {
        var command = CreateOrderCommand.create(UUID.randomUUID(), "Россия", "Москва", "Айтишная", "1", "10", 5)
                .getValue();
        var geoError = Error.of("geo.service.unavailable", "Сервис Geo недоступен");
        when(geoClient.getLocation(any())).thenReturn(Result.failure(geoError));

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(geoError);
        verify(orderRepository, never()).add(any());
    }

    @Test
    void create_whenVolumeIsNotPositive_shouldFail() {
        var command = CreateOrderCommand.create(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "10", 0);

        assertThat(command.isFailure()).isTrue();
    }
}
