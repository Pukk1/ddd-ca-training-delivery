package microarch.delivery.core.application.queries.getallcouriers;

import microarch.delivery.core.application.queries.common.LocationDto;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAllCouriersQueryHandlerImplTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final GetAllCouriersQueryHandler handler = new GetAllCouriersQueryHandlerImpl(courierRepository);

    @Test
    void handle_whenCouriersExist_shouldReturnDtoForEachCourier() {
        var ivan = Courier.create("Иван", Location.create(1, 2).getValue()).getValue();
        var petr = Courier.create("Пётр", Location.create(3, 4).getValue()).getValue();
        when(courierRepository.getAll()).thenReturn(List.of(ivan, petr));

        var result = handler.handle(GetAllCouriersQuery.create().getValue());

        assertThat(result).containsExactly(new CourierDto(ivan.getId(), "Иван", new LocationDto(1, 2)),
                new CourierDto(petr.getId(), "Пётр", new LocationDto(3, 4)));
    }

    @Test
    void handle_whenNoCouriers_shouldReturnEmptyList() {
        when(courierRepository.getAll()).thenReturn(List.of());

        var result = handler.handle(GetAllCouriersQuery.create().getValue());

        assertThat(result).isEmpty();
    }
}
