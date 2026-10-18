package microarch.delivery.core.application.commands.assignorder;

import libs.errs.Error;
import libs.errs.UnitResult;

public interface AssignOrderCommandHandler {
    UnitResult<Error> handle(AssignOrderCommand command);
}
