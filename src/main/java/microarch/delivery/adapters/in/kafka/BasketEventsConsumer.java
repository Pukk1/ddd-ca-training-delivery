package microarch.delivery.adapters.in.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.commands.createorder.CreateOrderCommand;
import microarch.delivery.core.application.commands.createorder.CreateOrderCommandHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import queues.basket.events.BasketEventsProto.BasketConfirmedIntegrationEvent;

import java.util.UUID;

@Slf4j
@Component
public class BasketEventsConsumer {
    private final CreateOrderCommandHandler createOrderCommandHandler;

    public BasketEventsConsumer(CreateOrderCommandHandler createOrderCommandHandler) {
        this.createOrderCommandHandler = createOrderCommandHandler;
    }

    @KafkaListener(topics = "${app.kafka.basket-events-topic}")
    public void consume(byte[] message) {
        BasketConfirmedIntegrationEvent event;
        UUID orderId;
        try {
            event = BasketConfirmedIntegrationEvent.parseFrom(message);
            orderId = UUID.fromString(event.getBasketId());
        } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
            log.error("Invalid basket event was skipped", e);
            return;
        }

        var address = event.getAddress();
        var command = CreateOrderCommand.create(orderId, address.getCountry(), address.getCity(), address.getStreet(),
                address.getHouse(), address.getApartment(), event.getVolume());
        if (command.isFailure()) {
            log.error("Invalid basket event {} was skipped: {}", orderId, command.getError().getMessage());
            return;
        }

        createOrderCommandHandler.handle(command.getValue())
                .onSuccess(() -> log.info("Order {} was created from basket event", orderId))
                .onFailure(error -> log.error("Order {} was not created: {}", orderId, error.getMessage()));
    }
}
