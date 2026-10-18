package microarch.delivery.core.application.commands.createcourier;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateCourierCommand {
    private final String name;

    public static Result<CreateCourierCommand, Error> create(String name) {
        var error = Guard.againstNullOrEmpty(name, "name");
        if (Objects.nonNull(error)) {
            return Result.failure(error);
        }

        return Result.success(new CreateCourierCommand(name));
    }
}
