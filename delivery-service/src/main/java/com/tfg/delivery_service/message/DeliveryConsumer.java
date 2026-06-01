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
        Long productionId = event.path("productionId").asLong();
        System.out.println("Event JSON: " + event.toString()); // temporal
        int amount = event.path("amount").asInt();
        System.out.println("Delivery receive: " + eventType + " deliveryId=" + deliveryId);

        try {
            // procecess event received
            processEvent(eventType, deliveryId, amount, productionId);
        } catch (Exception e) {
            System.out.println("Error processing event: " + e.getMessage());
            ///// Manage timeout 
            if (deliveryId != 0) {
                if (retryCount >= 2) {
                    deliveryService.getTimeoutState(deliveryId);
                }
            }
            throw e;
        }
    }
    
    // Process the event given. Case aproved or case rejected
    private void processEvent(String eventType, Long deliveryId, int amount, Long productionId) {
        switch (eventType) {
            // Case delivery in which can start delivery 
            case "delivery.accepted":
                deliveryService.startDelivery(deliveryId);
                break;
            // Compensating Transaction
            /*case "delivery.rejected":
                Delivery rejectedDelivery = deliveryService.getDelivery(deliveryId);
                deliveryService.rejectDelivery(rejectedDelivery);
                break;*/
            // case a new stock available
            case "stock.available":
                deliveryService.reserveDelivery(productionId, amount);
                break;
            case "create.delivery":
                deliveryService.createDeliveryFromStock(amount);
                break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }


}
