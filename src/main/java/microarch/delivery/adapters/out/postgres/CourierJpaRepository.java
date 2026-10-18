package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CourierJpaRepository extends JpaRepository<Courier, UUID> {
    // Агрегат загружается целиком вместе с назначениями одним запросом
    @Query("select distinct c from Courier c left join fetch c.assignments")
    List<Courier> findAllWithAssignments();
}
