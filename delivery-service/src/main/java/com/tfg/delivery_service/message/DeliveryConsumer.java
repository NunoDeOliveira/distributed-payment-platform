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

        // Extract data
        String eventType = event.path("eventType").asText();
        Long deliveryId = event.path("deliveryId").asLong();
        int amount = event.path("amount").asInt();
        System.out.println("Delivery receive: " + eventType + " deliveryId=" + deliveryId);

        try {
            ///// procecess event received
            processEvent(eventType, deliveryId);
        } catch (Exception e) {
            ///// Manage timeout or failed
            if (deliveryId != 0) {
                Delivery delivery = deliveryService.getDelivery(deliveryId);
                deliveryService.getFailedSate(delivery);
            }
            throw e;
        }
    }
    
    // Process the event given. Case aproved or case rejected
    private void processEvent(String eventType, Long deliveryId) {
        switch (eventType) {
            // Case delivery in which can start delivery 
            case "delivery.accepted":
                Delivery acceptedDelivery = deliveryService.getDelivery(deliveryId);
                deliveryService.startDelivery(acceptedDelivery);
                break;
            // Compensating Transaction
            case "delivery.rejected":
                Delivery rejectedDelivery = deliveryService.getDelivery(deliveryId);
                deliveryService.rejectDelivery(rejectedDelivery);
                break;
            // case a new stock available
            case "stock.available":
                deliveryService.processPendingDeliveries();
                break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }


}
