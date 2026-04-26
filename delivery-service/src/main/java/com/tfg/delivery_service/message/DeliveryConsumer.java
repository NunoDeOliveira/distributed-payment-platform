package com.tfg.delivery_service.message;

import org.springframework.stereotype.Component;
import com.tfg.delivery_service.service.DeliveryService;
import com.tfg.delivery_service.event.DeliveryEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@Component
public class DeliveryConsumer {

    private final DeliveryService deliveryService;

    public DeliveryConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // This method receive two type of events.
    // If event is approved the delivery starts,
    // if the event is reject start the compensating transaction
    @RabbitListener(queues = DeliveryPublish.DELIVERY_QUEUE)
    public void consume(DeliveryEvent event) {

        if ("production.approved".equals(event.getEvent())) {
            deliveryService.startDelivery(event.getDeliveryId());
        }

        if ("production.rejected".equals(event.getEvent())) {
            deliveryService.compensateRejectedDelivery(
                    event.getDeliveryId(), event.getAmount());
        }
    }
}
