package microarch.delivery.adapters.in.http;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.adapters.in.http.mapper.OrderDtoMapper;
import microarch.delivery.adapters.in.http.model.Address;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.commands.createorder.CreateOrderCommand;
import microarch.delivery.core.application.commands.createorder.CreateOrderCommandHandler;
import microarch.delivery.core.application.queries.common.LocationDto;
import microarch.delivery.core.application.queries.getnotcompletedorders.GetNotCompletedOrdersQueryHandler;
import microarch.delivery.core.application.queries.getnotcompletedorders.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderControllerTest {
    private static final Error HANDLER_ERROR = Error.of("test.error", "Test error");

    private CreateOrderCommandHandler createOrderCommandHandler;
    private GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler;
    private OrderDtoMapper orderDtoMapper;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        createOrderCommandHandler = mock(CreateOrderCommandHandler.class);
        getNotCompletedOrdersQueryHandler = mock(GetNotCompletedOrdersQueryHandler.class);
        orderDtoMapper = mock(OrderDtoMapper.class);
        controller = new OrderController(createOrderCommandHandler, getNotCompletedOrdersQueryHandler, orderDtoMapper);
    }

    @Test
    void createOrder_whenOrderIsValid_shouldReturnCreatedWithOrderId() {
        var orderId = UUID.randomUUID();
        when(createOrderCommandHandler.handle(any())).thenReturn(UnitResult.success());

        var response = controller.createOrder(createNewOrder(orderId, 5));

        var captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(createOrderCommandHandler).handle(captor.capture());
        var command = captor.getValue();
        assertThat(command.getOrderId()).isEqualTo(orderId);
        assertThat(command.getCountry()).isEqualTo("Россия");
        assertThat(command.getCity()).isEqualTo("Москва");
        assertThat(command.getStreet()).isEqualTo("Тверская");
        assertThat(command.getHouse()).isEqualTo("1");
        assertThat(command.getApartment()).isEqualTo("2");
        assertThat(command.getVolume()).isEqualTo(5);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getOrderId()).isEqualTo(orderId);
    }

    @Test
    void createOrder_whenVolumeIsZero_shouldThrowBadRequestWithoutHandling() {
        assertThatThrownBy(() -> controller.createOrder(createNewOrder(UUID.randomUUID(), 0))).isInstanceOfSatisfying(
                ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(createOrderCommandHandler, never()).handle(any());
    }

    @Test
    void createOrder_whenHandlerFails_shouldThrowConflict() {
        when(createOrderCommandHandler.handle(any())).thenReturn(UnitResult.failure(HANDLER_ERROR));

        assertThatThrownBy(() -> controller.createOrder(createNewOrder(UUID.randomUUID(), 5)))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getError()).isEqualTo(HANDLER_ERROR);
                });
    }

    @Test
    void getOrders_whenNotCompletedOrdersExist_shouldReturnOkWithMappedOrders() {
        var dtos = List.of(new OrderDto(UUID.randomUUID(), new LocationDto(1, 2)));
        var orders = List.of(new Order(dtos.getFirst().id(), new Location(1, 2)));
        when(getNotCompletedOrdersQueryHandler.handle(any())).thenReturn(dtos);
        when(orderDtoMapper.fromDtos(dtos)).thenReturn(orders);

        var response = controller.getOrders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orders);
    }

    private NewOrder createNewOrder(UUID orderId, int volume) {
        return new NewOrder(orderId, new Address("Россия", "Москва", "Тверская", "1", "2"), volume);
    }
}
