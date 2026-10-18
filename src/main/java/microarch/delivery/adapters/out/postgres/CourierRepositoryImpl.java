package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CourierRepositoryImpl implements CourierRepository {
    private final CourierJpaRepository jpa;

    public CourierRepositoryImpl(CourierJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void add(Courier courier) {
        jpa.save(courier);
    }

    @Override
    public void update(Courier courier) {
        jpa.save(courier);
    }

    @Override
    public Optional<Courier> getById(UUID courierId) {
        return jpa.findById(courierId);
    }

    @Override
    public List<Courier> getAll() {
        return jpa.findAllWithAssignments();
    }
}
