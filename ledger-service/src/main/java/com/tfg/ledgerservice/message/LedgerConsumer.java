package com.tfg.ledgerservice.message;

import org.springframework.stereotype.Component;
import com.tfg.ledgerservice.service.LedgerService;
import com.tfg.ledgerservice.model.LedgerMovement;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import com.fasterxml.jackson.databind.JsonNode;


@Component
public class LedgerConsumer {

    private final LedgerService deliveryService;

    public LedgerConsumer(LedgerService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = LedgerPublish.DELIVERY_QUEUE)
    public void consume(JsonNode event,
        @Header(value = "x-delivery-count", defaultValue = "0") int retryCount) {

        // Extract data
        String eventType = event.path("eventType").asText();
        Long deliveryId = event.path("deliveryId").asLong();
        Long productionId = event.path("productionId").asLong();
        System.out.println("Event JSON: " + event.toString()); // temporal
        int amount = event.path("amount").asInt();
        System.out.println("LedgerMovement receive: " + eventType + " deliveryId=" + deliveryId);

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
                deliveryService.startLedgerMovement(deliveryId);
                break;
            case "delivery.cancel":
                deliveryService.cancelLedgerMovementByProductionId(productionId);
                break;
            case "stock.available":
                deliveryService.reserveLedgerMovement(productionId, amount);
                break;
            //case "create.delivery":
                //deliveryService.createLedgerMovementFromStock(amount);
                //break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }


}
