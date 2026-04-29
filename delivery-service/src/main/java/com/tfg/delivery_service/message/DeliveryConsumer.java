package com.tfg.delivery_service.message;

import org.springframework.stereotype.Component;
import com.tfg.delivery_service.service.DeliveryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class DeliveryConsumer {

    private final DeliveryService deliveryService;

    public DeliveryConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = DeliveryPublish.DELIVERY_QUEUE)
    public void consume(JsonNode event) {
        // Extracting data using Jackson Path API
        String eventType = event.path("eventType").asText();
        Long deliveryId = event.path("productionId").asLong();
        int amount = event.path("amount").asInt();

        System.out.println("Delivery receive: " + event.toString());
        
        // Case delivery in which can start delivery 
        if ("delivery.accepted".equals(eventType)) {
            deliveryService.startDelivery(deliveryId);
        }
        
        // Compensating Transaction
        if ("delivery.rejected".equals(eventType)) {
            deliveryService.compensateRejectedDelivery(deliveryId, amount);
        }
    }
}
