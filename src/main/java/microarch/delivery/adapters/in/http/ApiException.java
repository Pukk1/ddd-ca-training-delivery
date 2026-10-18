package microarch.delivery.adapters.in.http;

import libs.errs.Error;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final transient Error error;

    private ApiException(HttpStatus status, Error error) {
        super(error.getMessage());
        this.status = status;
        this.error = error;
    }

    public static ApiException badRequest(Error error) {
        return new ApiException(HttpStatus.BAD_REQUEST, error);
    }

    public static ApiException conflict(Error error) {
        return new ApiException(HttpStatus.CONFLICT, error);
    }
}
