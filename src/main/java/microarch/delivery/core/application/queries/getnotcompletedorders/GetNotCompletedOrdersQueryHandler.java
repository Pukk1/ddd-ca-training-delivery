package microarch.delivery.core.application.queries.getnotcompletedorders;

import java.util.List;

public interface GetNotCompletedOrdersQueryHandler {
    List<OrderDto> handle(GetNotCompletedOrdersQuery query);
}
