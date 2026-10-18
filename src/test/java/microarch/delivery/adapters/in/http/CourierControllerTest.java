package microarch.delivery.adapters.in.http;

import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.adapters.in.http.mapper.CourierDtoMapper;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.adapters.in.http.model.NewCourier;
import microarch.delivery.core.application.commands.completeorder.CompleteOrderCommand;
import microarch.delivery.core.application.commands.completeorder.CompleteOrderCommandHandler;
import microarch.delivery.core.application.commands.createcourier.CreateCourierCommand;
import microarch.delivery.core.application.commands.createcourier.CreateCourierCommandHandler;
import microarch.delivery.core.application.commands.movecourier.MoveCourierCommand;
import microarch.delivery.core.application.commands.movecourier.MoveCourierCommandHandler;
import microarch.delivery.core.application.queries.common.LocationDto;
import microarch.delivery.core.application.queries.getallcouriers.CourierDto;
import microarch.delivery.core.application.queries.getallcouriers.GetAllCouriersQueryHandler;
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

class CourierControllerTest {
    private static final Error HANDLER_ERROR = Error.of("test.error", "Test error");

    private CreateCourierCommandHandler createCourierCommandHandler;
    private GetAllCouriersQueryHandler getAllCouriersQueryHandler;
    private MoveCourierCommandHandler moveCourierCommandHandler;
    private CompleteOrderCommandHandler completeOrderCommandHandler;
    private CourierDtoMapper courierDtoMapper;
    private CourierController controller;

    @BeforeEach
    void setUp() {
        createCourierCommandHandler = mock(CreateCourierCommandHandler.class);
        getAllCouriersQueryHandler = mock(GetAllCouriersQueryHandler.class);
        moveCourierCommandHandler = mock(MoveCourierCommandHandler.class);
        completeOrderCommandHandler = mock(CompleteOrderCommandHandler.class);
        courierDtoMapper = mock(CourierDtoMapper.class);
        controller = new CourierController(createCourierCommandHandler, getAllCouriersQueryHandler,
                moveCourierCommandHandler, completeOrderCommandHandler, courierDtoMapper);
    }

    @Test
    void createCourier_whenNameIsValid_shouldReturnCreatedWithCourierId() {
        var courierId = UUID.randomUUID();
        when(createCourierCommandHandler.handle(any())).thenReturn(Result.success(courierId));

        var response = controller.createCourier(new NewCourier("Иван"));

        var captor = ArgumentCaptor.forClass(CreateCourierCommand.class);
        verify(createCourierCommandHandler).handle(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Иван");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCourierId()).isEqualTo(courierId);
    }

    @Test
    void createCourier_whenNameIsEmpty_shouldThrowBadRequestWithoutHandling() {
        assertThatThrownBy(() -> controller.createCourier(new NewCourier(""))).isInstanceOfSatisfying(
                ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(createCourierCommandHandler, never()).handle(any());
    }

    @Test
    void createCourier_whenHandlerFails_shouldThrowConflict() {
        when(createCourierCommandHandler.handle(any())).thenReturn(Result.failure(HANDLER_ERROR));

        assertThatThrownBy(() -> controller.createCourier(new NewCourier("Иван")))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getError()).isEqualTo(HANDLER_ERROR);
                });
    }

    @Test
    void getCouriers_whenCouriersExist_shouldReturnOkWithMappedCouriers() {
        var dtos = List.of(new CourierDto(UUID.randomUUID(), "Иван", new LocationDto(1, 2)));
        var couriers = List.of(new Courier(dtos.getFirst().id(), "Иван", new Location(1, 2)));
        when(getAllCouriersQueryHandler.handle(any())).thenReturn(dtos);
        when(courierDtoMapper.fromDtos(dtos)).thenReturn(couriers);

        var response = controller.getCouriers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(couriers);
    }

    @Test
    void moveCourier_whenLocationIsValid_shouldReturnOkAndMoveCourier() {
        var courierId = UUID.randomUUID();
        when(moveCourierCommandHandler.handle(any())).thenReturn(UnitResult.success());

        var response = controller.moveCourier(courierId, new Location(3, 4));

        var captor = ArgumentCaptor.forClass(MoveCourierCommand.class);
        verify(moveCourierCommandHandler).handle(captor.capture());
        assertThat(captor.getValue().getCourierId()).isEqualTo(courierId);
        assertThat(captor.getValue().getLocation().getX()).isEqualTo(3);
        assertThat(captor.getValue().getLocation().getY()).isEqualTo(4);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void moveCourier_whenLocationIsOutOfRange_shouldThrowBadRequestWithoutHandling() {
        assertThatThrownBy(() -> controller.moveCourier(UUID.randomUUID(), new Location(0, 4))).isInstanceOfSatisfying(
                ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(moveCourierCommandHandler, never()).handle(any());
    }

    @Test
    void moveCourier_whenHandlerFails_shouldThrowConflict() {
        when(moveCourierCommandHandler.handle(any())).thenReturn(UnitResult.failure(HANDLER_ERROR));

        assertThatThrownBy(() -> controller.moveCourier(UUID.randomUUID(), new Location(3, 4)))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getError()).isEqualTo(HANDLER_ERROR);
                });
    }

    @Test
    void completeOrder_whenHandlerSucceeds_shouldReturnOkAndCompleteOrder() {
        var courierId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(completeOrderCommandHandler.handle(any())).thenReturn(UnitResult.success());

        var response = controller.completeOrder(courierId, orderId);

        var captor = ArgumentCaptor.forClass(CompleteOrderCommand.class);
        verify(completeOrderCommandHandler).handle(captor.capture());
        assertThat(captor.getValue().getCourierId()).isEqualTo(courierId);
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void completeOrder_whenHandlerFails_shouldThrowConflict() {
        when(completeOrderCommandHandler.handle(any())).thenReturn(UnitResult.failure(HANDLER_ERROR));

        assertThatThrownBy(() -> controller.completeOrder(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getError()).isEqualTo(HANDLER_ERROR);
                });
    }
}
