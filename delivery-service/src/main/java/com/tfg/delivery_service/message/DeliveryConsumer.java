package com.tfg.delivery_service.message;

import org.springframework.stereotype.Component;
import com.tfg.delivery_service.service.DeliveryService;
import com.tfg.delivery_service.model.Delivery;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import com.fasterxml.jackson.databind.JsonNode;


@Component
public class DeliveryConsumer {

    private final DeliveryService deliveryService;

    public DeliveryConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = DeliveryPublish.DELIVERY_QUEUE)
    public void consume(JsonNode event,
            @Header(value = "x-delivery-count", defaultValue = "0") int retryCount) {

        String eventType = event.path("eventType").asText();
        Long deliveryId = event.path("deliveryId").asLong();
        int amount = event.path("amount").asInt();

        System.out.println("Delivery receive: " + eventType + " deliveryId=" + deliveryId);

        try {
            processEvent(eventType, deliveryId, amount);
        } catch (Exception e) {
            if (deliveryId != 0) {
                Delivery delivery = deliveryService.getDelivery(deliveryId);
                // count retries, if the retries are less than 2, get timeout
                if (retryCount < 2) {
                    deliveryService.getTimeoutState(delivery);
                } else {
                    // else get failed status 
                    deliveryService.getFailedSate(delivery);
                }
            }
            throw e;
        }
    }

    private void processEvent(String eventType, Long deliveryId, int amount) {
        switch (eventType) {
            // Case delivery in which can start delivery 
            case "delivery.accepted":
            // Compensating Transaction
            case "delivery.rejected":
                Delivery delivery = deliveryService.getDelivery(deliveryId);
                if ("delivery.accepted".equals(eventType)) {
                    deliveryService.startDelivery(delivery);
                } else {
                    deliveryService.compensateRejectedDelivery(delivery, amount);
                }
                break;
            case "stock.available":
                deliveryService.startNextPending(amount);
                break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }


}
