package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.volume.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CourierRepositoryIntegrationTest extends BasePostgresContainerTest {

    @Autowired
    CourierRepository repository;

    @Autowired
    CourierJpaRepository jpa;

    @BeforeEach
    void cleanUp() {
        jpa.deleteAll();
    }

    @Test
    void add_whenNewCourier_shouldBeFoundById() {
        var courier = createCourier("Иван");

        repository.add(courier);
        var loaded = repository.getById(courier.getId());

        assertThat(loaded).isPresent();
        assertThat(loaded.get()).usingRecursiveComparison().isEqualTo(courier);
    }

    @Test
    void update_whenOrderAssignedToCourier_shouldPersistAssignment() {
        var courier = createCourier("Иван");
        repository.add(courier);

        courier.assignNewOrder(createOrder());
        repository.update(courier);
        var loaded = repository.getById(courier.getId());

        assertThat(loaded).isPresent();
        assertThat(loaded.get()).usingRecursiveComparison().isEqualTo(courier);
    }

    @Test
    void getAll_whenSeveralCouriersWithAssignments_shouldReturnAllCouriers() {
        var firstCourier = createCourier("Иван");
        firstCourier.assignNewOrder(createOrder());
        firstCourier.assignNewOrder(createOrder());
        var secondCourier = createCourier("Пётр");
        repository.add(firstCourier);
        repository.add(secondCourier);

        var loaded = repository.getAll();

        assertThat(loaded).extracting(Courier::getId).containsExactlyInAnyOrder(firstCourier.getId(),
                secondCourier.getId());
        // Порядок назначений в БД не хранится, поэтому коллекции сравниваются без учёта порядка
        for (var expected : List.of(firstCourier, secondCourier)) {
            var actual = loaded.stream().filter(it -> it.getId().equals(expected.getId())).findFirst().orElseThrow();
            assertThat(actual).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expected);
        }
    }

    private Courier createCourier(String name) {
        return Courier.create(name, Location.create(1, 1).getValue()).getValue();
    }

    private Order createOrder() {
        return Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();
    }
}
