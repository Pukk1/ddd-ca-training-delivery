package microarch.delivery.config;

import clients.geo.GeoGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoGrpcConfig {

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel geoChannel(@Value("${app.grpc.geo-service.host}") String host,
            @Value("${app.grpc.geo-service.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    public GeoGrpc.GeoBlockingStub geoBlockingStub(ManagedChannel geoChannel) {
        return GeoGrpc.newBlockingStub(geoChannel);
    }
}
