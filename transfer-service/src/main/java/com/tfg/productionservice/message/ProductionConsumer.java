package com.tfg.productionservice.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.productionservice.service.ProductionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import com.tfg.productionservice.model.Production;
import org.springframework.stereotype.Component;

@Component
public class ProductionConsumer {

    private final ProductionService productionService;

    public ProductionConsumer(ProductionService productionService) {
        this.productionService = productionService;
    }
    
    @RabbitListener(queues = ProductionPublish.PRODUCTION_QUEUE)
    public void consume(JsonNode event, 
        @Header(value = "x-delivery-count", defaultValue = "0") int retryCount) {
        //System.out.println("Message received: " + event.toString());
        
        // Extract data
        String eventType = event.path("eventType").asText();
        Long productionId = event.path("productionId").asLong();
        int amountAllowed = event.path("amount").asInt();   // this is the amount allowed
        System.out.println("EventType: " + eventType + "ProductionId: " + productionId);

        try {
            // procecess event received
            processEvent(eventType, productionId, amountAllowed);
        } catch (Exception e) {
            ///// Manage timeout or failed
            if (productionId != 0) {
                if (retryCount >= 2) {
                    // third trying release
                    productionService.getTimeoutState(productionId); 
                }
            }
            throw e;
        }       
    }
    
    // Process the event given. Case aproved or case rejected
    private void processEvent(String eventType, Long productionId, int amountAllowed) {
        switch (eventType) {
            case "production.accepted":
                Production production = productionService.getProduction(productionId);
                productionService.startProduction(productionId);
                break;
            case "production.rejected":
                Production productionRejected = productionService.getProduction(productionId);
                productionService.rejectProduction(productionRejected, amountAllowed);
                break;
            case "production.cancelled":
                productionService.cancelProduction(productionId);
                break;
            //case "stock.available":
                //productionService.processPendingProductions();
                //break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
        
}
