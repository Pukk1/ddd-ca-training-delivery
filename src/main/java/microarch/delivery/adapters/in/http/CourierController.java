package microarch.delivery.adapters.in.http;

import microarch.delivery.adapters.in.http.api.CompleteOrderApi;
import microarch.delivery.adapters.in.http.api.CreateCourierApi;
import microarch.delivery.adapters.in.http.api.GetCouriersApi;
import microarch.delivery.adapters.in.http.api.MoveCourierApi;
import microarch.delivery.adapters.in.http.mapper.CourierDtoMapper;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.adapters.in.http.model.CreateCourierResponse;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.adapters.in.http.model.NewCourier;
import microarch.delivery.core.application.commands.completeorder.CompleteOrderCommand;
import microarch.delivery.core.application.commands.completeorder.CompleteOrderCommandHandler;
import microarch.delivery.core.application.commands.createcourier.CreateCourierCommand;
import microarch.delivery.core.application.commands.createcourier.CreateCourierCommandHandler;
import microarch.delivery.core.application.commands.movecourier.MoveCourierCommand;
import microarch.delivery.core.application.commands.movecourier.MoveCourierCommandHandler;
import microarch.delivery.core.application.queries.getallcouriers.GetAllCouriersQuery;
import microarch.delivery.core.application.queries.getallcouriers.GetAllCouriersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class CourierController implements CreateCourierApi, GetCouriersApi, MoveCourierApi, CompleteOrderApi {
    private final CreateCourierCommandHandler createCourierCommandHandler;
    private final GetAllCouriersQueryHandler getAllCouriersQueryHandler;
    private final MoveCourierCommandHandler moveCourierCommandHandler;
    private final CompleteOrderCommandHandler completeOrderCommandHandler;
    private final CourierDtoMapper courierDtoMapper;

    public CourierController(CreateCourierCommandHandler createCourierCommandHandler,
            GetAllCouriersQueryHandler getAllCouriersQueryHandler, MoveCourierCommandHandler moveCourierCommandHandler,
            CompleteOrderCommandHandler completeOrderCommandHandler, CourierDtoMapper courierDtoMapper) {
        this.createCourierCommandHandler = createCourierCommandHandler;
        this.getAllCouriersQueryHandler = getAllCouriersQueryHandler;
        this.moveCourierCommandHandler = moveCourierCommandHandler;
        this.completeOrderCommandHandler = completeOrderCommandHandler;
        this.courierDtoMapper = courierDtoMapper;
    }

    @Override
    public ResponseEntity<CreateCourierResponse> createCourier(NewCourier newCourier) {
        var command = CreateCourierCommand.create(newCourier.getName());
        if (command.isFailure()) {
            throw ApiException.badRequest(command.getError());
        }

        var courierId = createCourierCommandHandler.handle(command.getValue());
        if (courierId.isFailure()) {
            throw ApiException.conflict(courierId.getError());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateCourierResponse(courierId.getValue()));
    }

    @Override
    public ResponseEntity<List<Courier>> getCouriers() {
        var couriers = getAllCouriersQueryHandler.handle(GetAllCouriersQuery.create().getValue());

        return ResponseEntity.ok(courierDtoMapper.fromDtos(couriers));
    }

    @Override
    public ResponseEntity<Void> moveCourier(UUID courierId, Location location) {
        var target = microarch.delivery.core.domain.model.kernel.Location.create(location.getX(), location.getY());
        if (target.isFailure()) {
            throw ApiException.badRequest(target.getError());
        }

        var command = MoveCourierCommand.create(courierId, target.getValue());
        if (command.isFailure()) {
            throw ApiException.badRequest(command.getError());
        }

        moveCourierCommandHandler.handle(command.getValue()).getOrElseThrow(ApiException::conflict);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> completeOrder(UUID courierId, UUID orderId) {
        var command = CompleteOrderCommand.create(courierId, orderId);
        if (command.isFailure()) {
            throw ApiException.badRequest(command.getError());
        }

        completeOrderCommandHandler.handle(command.getValue()).getOrElseThrow(ApiException::conflict);

        return ResponseEntity.ok().build();
    }
}
