package microarch.delivery.core.application.commands.movecourier;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MoveCourierCommandHandlerImplTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final MoveCourierCommandHandler handler = new MoveCourierCommandHandlerImpl(courierRepository);

    @Test
    void handle_whenMoveIsOneStep_shouldMoveCourierAndUpdate() {
        var courier = Courier.create("Иван", Location.create(1, 1).getValue()).getValue();
        var target = Location.create(1, 2).getValue();
        when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));

        var result = handler.handle(MoveCourierCommand.create(courier.getId(), target).getValue());

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(target);
        verify(courierRepository).update(courier);
    }

    @Test
    void handle_whenCourierNotFound_shouldFailWithoutUpdate() {
        var courierId = UUID.randomUUID();
        when(courierRepository.getById(courierId)).thenReturn(Optional.empty());

        var result = handler.handle(MoveCourierCommand.create(courierId, Location.create(1, 2).getValue()).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.notFound("courier", courierId));
        verify(courierRepository, never()).update(any());
    }

    @Test
    void handle_whenMoveIsLongerThanOneStep_shouldFailAndKeepLocation() {
        var start = Location.create(1, 1).getValue();
        var courier = Courier.create("Иван", start).getValue();
        when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));

        var result = handler
                .handle(MoveCourierCommand.create(courier.getId(), Location.create(3, 3).getValue()).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Courier.Errors.onlyOneStepMove());
        assertThat(courier.getLocation()).isEqualTo(start);
        verify(courierRepository, never()).update(any());
    }
}
