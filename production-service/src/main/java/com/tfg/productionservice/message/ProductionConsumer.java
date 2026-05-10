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
        System.out.println("Message received: " + event.toString());

        String eventType = event.path("eventType").asText();
        Long productionId = event.path("productionId").asLong();
        int amount = event.path("amount").asInt();

        System.out.println("EventType: " + eventType + "ProductionId: " + productionId);
        Production production = productionService.getProduction(productionId);
        
        try {
            if ("production.approved".equals(eventType)) {
                productionService.startProduction(production);
            }
            if ("production.rejected".equals(eventType)) {
                productionService.compensateRejectedProduction(production, amount);
            }
        } catch (Exception e) {
            // count retries, if the retries are less than 2, get timeout
            if (retryCount < 2) {
                productionService.getTimeoutState(production);
            // else get failed status 
            } else {
                productionService.getFailedSate(production);
            } 
            throw e;
        }       
    }
}
