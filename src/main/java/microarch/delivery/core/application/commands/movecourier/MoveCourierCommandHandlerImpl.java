package microarch.delivery.core.application.commands.movecourier;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoveCourierCommandHandlerImpl implements MoveCourierCommandHandler {
    private final CourierRepository courierRepository;

    public MoveCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(MoveCourierCommand command) {
        var courierOptional = courierRepository.getById(command.getCourierId());
        if (courierOptional.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        }
        var courier = courierOptional.get();

        var moveResult = courier.moveTo(command.getLocation());
        if (moveResult.isFailure()) {
            return moveResult;
        }

        courierRepository.update(courier);
        return UnitResult.success();
    }
}
