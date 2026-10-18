package microarch.delivery.adapters.in.http;

import microarch.delivery.adapters.in.http.api.CreateOrderApi;
import microarch.delivery.adapters.in.http.api.GetOrdersApi;
import microarch.delivery.adapters.in.http.mapper.OrderDtoMapper;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.commands.createorder.CreateOrderCommand;
import microarch.delivery.core.application.commands.createorder.CreateOrderCommandHandler;
import microarch.delivery.core.application.queries.getnotcompletedorders.GetNotCompletedOrdersQuery;
import microarch.delivery.core.application.queries.getnotcompletedorders.GetNotCompletedOrdersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController implements CreateOrderApi, GetOrdersApi {
    private final CreateOrderCommandHandler createOrderCommandHandler;
    private final GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler;
    private final OrderDtoMapper orderDtoMapper;

    public OrderController(CreateOrderCommandHandler createOrderCommandHandler,
            GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler, OrderDtoMapper orderDtoMapper) {
        this.createOrderCommandHandler = createOrderCommandHandler;
        this.getNotCompletedOrdersQueryHandler = getNotCompletedOrdersQueryHandler;
        this.orderDtoMapper = orderDtoMapper;
    }

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(NewOrder newOrder) {
        var address = newOrder.getAddress();
        var command = CreateOrderCommand.create(newOrder.getId(), address.getCountry(), address.getCity(),
                address.getStreet(), address.getHouse(), address.getApartment(), newOrder.getVolume());
        if (command.isFailure()) {
            throw ApiException.badRequest(command.getError());
        }

        createOrderCommandHandler.handle(command.getValue()).getOrElseThrow(ApiException::conflict);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateOrderResponse(command.getValue().getOrderId()));
    }

    @Override
    public ResponseEntity<List<Order>> getOrders() {
        var orders = getNotCompletedOrdersQueryHandler.handle(GetNotCompletedOrdersQuery.create().getValue());

        return ResponseEntity.ok(orderDtoMapper.fromDtos(orders));
    }
}
