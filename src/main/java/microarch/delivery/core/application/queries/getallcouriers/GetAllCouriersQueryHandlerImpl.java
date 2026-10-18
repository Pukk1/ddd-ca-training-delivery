package microarch.delivery.core.application.queries.getallcouriers;

import microarch.delivery.core.application.queries.common.LocationDto;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {
    private final CourierRepository courierRepository;

    public GetAllCouriersQueryHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierDto> handle(GetAllCouriersQuery query) {
        return courierRepository.getAll().stream().map(
                courier -> new CourierDto(courier.getId(), courier.getName(), LocationDto.from(courier.getLocation())))
                .toList();
    }
}
