package microarch.delivery.adapters.in.kafka;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.application.commands.createorder.CreateOrderCommand;
import microarch.delivery.core.application.commands.createorder.CreateOrderCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import queues.basket.events.BasketEventsProto.Address;
import queues.basket.events.BasketEventsProto.BasketConfirmedIntegrationEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BasketEventsConsumerTest {
    private CreateOrderCommandHandler createOrderCommandHandler;
    private BasketEventsConsumer consumer;

    @BeforeEach
    void setUp() {
        createOrderCommandHandler = mock(CreateOrderCommandHandler.class);
        consumer = new BasketEventsConsumer(createOrderCommandHandler);
    }

    @Test
    void consume_whenEventIsValid_shouldCreateOrder() {
        var basketId = UUID.randomUUID();
        when(createOrderCommandHandler.handle(any())).thenReturn(UnitResult.success());

        consumer.consume(createEvent(basketId.toString(), 5).toByteArray());

        var captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(createOrderCommandHandler).handle(captor.capture());
        var command = captor.getValue();
        assertThat(command.getOrderId()).isEqualTo(basketId);
        assertThat(command.getCountry()).isEqualTo("Россия");
        assertThat(command.getCity()).isEqualTo("Москва");
        assertThat(command.getStreet()).isEqualTo("Тверская");
        assertThat(command.getHouse()).isEqualTo("1");
        assertThat(command.getApartment()).isEqualTo("2");
        assertThat(command.getVolume()).isEqualTo(5);
    }

    @Test
    void consume_whenHandlerFails_shouldNotThrow() {
        when(createOrderCommandHandler.handle(any())).thenReturn(UnitResult.failure(Error.of("test.error", "Test")));
        var message = createEvent(UUID.randomUUID().toString(), 5).toByteArray();

        assertThatCode(() -> consumer.consume(message)).doesNotThrowAnyException();
    }

    @Test
    void consume_whenMessageIsNotProtobuf_shouldSkipEvent() {
        consumer.consume("not a protobuf".getBytes(StandardCharsets.UTF_8));

        verify(createOrderCommandHandler, never()).handle(any());
    }

    @Test
    void consume_whenBasketIdIsNotUuid_shouldSkipEvent() {
        consumer.consume(createEvent("not-uuid", 5).toByteArray());

        verify(createOrderCommandHandler, never()).handle(any());
    }

    @Test
    void consume_whenVolumeIsInvalid_shouldSkipEvent() {
        consumer.consume(createEvent(UUID.randomUUID().toString(), 0).toByteArray());

        verify(createOrderCommandHandler, never()).handle(any());
    }

    private BasketConfirmedIntegrationEvent createEvent(String basketId, int volume) {
        return BasketConfirmedIntegrationEvent
                .newBuilder().setBasketId(basketId).setAddress(Address.newBuilder().setCountry("Россия")
                        .setCity("Москва").setStreet("Тверская").setHouse("1").setApartment("2"))
                .setVolume(volume).build();
    }
}
