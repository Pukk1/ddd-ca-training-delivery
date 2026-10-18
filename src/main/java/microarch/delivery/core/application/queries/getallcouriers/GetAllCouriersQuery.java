package microarch.delivery.core.application.queries.getallcouriers;

import libs.errs.Error;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetAllCouriersQuery {

    public static Result<GetAllCouriersQuery, Error> create() {
        return Result.success(new GetAllCouriersQuery());
    }
}
