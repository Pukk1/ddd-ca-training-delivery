package microarch.delivery.core.application.queries.getnotcompletedorders;

import libs.errs.Error;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetNotCompletedOrdersQuery {

    public static Result<GetNotCompletedOrdersQuery, Error> create() {
        return Result.success(new GetNotCompletedOrdersQuery());
    }
}
