package microarch.delivery.config;

import microarch.delivery.core.domain.services.OrderAssignation;
import microarch.delivery.core.domain.services.OrderAssignationImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Доменные сервисы не зависят от Spring, поэтому регистрируются как бины здесь
@Configuration
public class DomainServicesConfig {

    @Bean
    public OrderAssignation orderAssignation() {
        return new OrderAssignationImpl();
    }
}
