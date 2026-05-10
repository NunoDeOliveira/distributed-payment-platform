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
        
        // Extracting data using Jackson Path API
        String eventType = event.path("eventType").asText();
        Long deliveryId = event.path("productionId").asLong();
        int amount = event.path("amount").asInt();

        System.out.println("Delivery receive: " + eventType + " deliveryId=" + deliveryId);
        
        Delivery delivery = deliveryService.getDelivery(deliveryId);
        
        try {
            // Case delivery in which can start delivery 
            if ("delivery.accepted".equals(eventType)) {
                deliveryService.startDelivery(delivery);
            }
            
            // Compensating Transaction
            if ("delivery.rejected".equals(eventType)) {
                deliveryService.compensateRejectedDelivery(delivery, amount);
            }
        } catch (Exception e) {
            // count retries, if the retries are less than 2, get timeout
            if (retryCount < 2) {
                deliveryService.getTimeoutState(delivery);
            // else get failed status 
            } else {
                deliveryService.getFailedSate(delivery);   
            } 
            throw e;
        }         
    }
}
