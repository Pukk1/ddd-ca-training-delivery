package microarch.delivery.adapters.out.grpc;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto.GetGeolocationRequest;
import io.grpc.StatusRuntimeException;
import libs.errs.Error;
import libs.errs.Result;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.GeoClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GeoClientImpl implements GeoClient {
    private static final long DEADLINE_SECONDS = 5;

    private final GeoGrpc.GeoBlockingStub geoStub;

    public GeoClientImpl(GeoGrpc.GeoBlockingStub geoStub) {
        this.geoStub = geoStub;
    }

    @Override
    public Result<Location, Error> getLocation(String street) {
        var request = GetGeolocationRequest.newBuilder().setStreet(street).build();
        try {
            var reply = geoStub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS).getGeolocation(request);
            return Location.create(reply.getLocation().getX(), reply.getLocation().getY());
        } catch (StatusRuntimeException ex) {
            log.warn("Geo service call failed: {}", ex.getStatus());
            return Result.failure(Errors.geoServiceUnavailable());
        }
    }

    public static class Errors {
        public static Error geoServiceUnavailable() {
            return Error.of("geo.service.unavailable", "Сервис Geo недоступен");
        }
    }
}
