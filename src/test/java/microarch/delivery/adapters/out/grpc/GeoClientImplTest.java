package microarch.delivery.adapters.out.grpc;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import microarch.delivery.core.domain.model.kernel.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeoClientImplTest {

    private GeoGrpc.GeoBlockingStub geoStub;
    private GeoClientImpl geoClient;

    @BeforeEach
    void setUp() {
        geoStub = mock(GeoGrpc.GeoBlockingStub.class);
        when(geoStub.withDeadlineAfter(anyLong(), any(TimeUnit.class))).thenReturn(geoStub);
        geoClient = new GeoClientImpl(geoStub);
    }

    @Test
    void getLocation_whenGeoReturnsLocation_shouldReturnDomainLocation() {
        when(geoStub.getGeolocation(any())).thenReturn(reply(3, 3));

        var result = geoClient.getLocation("Эйчарная");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(Location.create(3, 3).getValue());
        var captor = ArgumentCaptor.forClass(GeoProto.GetGeolocationRequest.class);
        verify(geoStub).getGeolocation(captor.capture());
        assertThat(captor.getValue().getStreet()).isEqualTo("Эйчарная");
    }

    @Test
    void getLocation_whenCalled_shouldSetDeadlineOnStub() {
        when(geoStub.getGeolocation(any())).thenReturn(reply(1, 1));

        geoClient.getLocation("Тестировочная");

        verify(geoStub).withDeadlineAfter(5, TimeUnit.SECONDS);
    }

    @Test
    void getLocation_whenGeoReturnsLocationOutOfRange_shouldFail() {
        when(geoStub.getGeolocation(any())).thenReturn(reply(0, 11));

        var result = geoClient.getLocation("Эйчарная");

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void getLocation_whenGeoIsUnavailable_shouldFailWithGeoServiceUnavailable() {
        when(geoStub.getGeolocation(any())).thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        var result = geoClient.getLocation("Эйчарная");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeoClientImpl.Errors.geoServiceUnavailable());
    }

    private GeoProto.GetGeolocationReply reply(int x, int y) {
        return GeoProto.GetGeolocationReply.newBuilder().setLocation(GeoProto.Location.newBuilder().setX(x).setY(y))
                .build();
    }
}
