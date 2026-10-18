package microarch.delivery.core.application.commands.createcourier;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {
    private final CourierRepository courierRepository;

    public CreateCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateCourierCommand command) {
        var courier = Courier.create(command.getName(), Location.createRandom());
        if (courier.isFailure()) {
            return Result.failure(courier.getError());
        }

        courierRepository.add(courier.getValue());
        return Result.success(courier.getValue().getId());
    }
}
