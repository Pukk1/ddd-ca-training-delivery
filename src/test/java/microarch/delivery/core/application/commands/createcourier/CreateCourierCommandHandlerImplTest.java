package microarch.delivery.core.application.commands.createcourier;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CreateCourierCommandHandlerImplTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final CreateCourierCommandHandler handler = new CreateCourierCommandHandlerImpl(courierRepository);

    @Test
    void handle_whenCommandIsValid_shouldAddCourierAndReturnItsId() {
        var command = CreateCourierCommand.create("Иван").getValue();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        var captor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository).add(captor.capture());
        var courier = captor.getValue();
        assertThat(result.getValue()).isEqualTo(courier.getId());
        assertThat(courier.getName()).isEqualTo("Иван");
        assertThat(courier.getLocation().getX()).isBetween(1, 10);
        assertThat(courier.getLocation().getY()).isBetween(1, 10);
    }

    @Test
    void create_whenNameIsEmpty_shouldFail() {
        var command = CreateCourierCommand.create("");

        assertThat(command.isFailure()).isTrue();
    }
}
