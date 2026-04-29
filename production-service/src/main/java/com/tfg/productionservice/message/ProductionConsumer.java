package com.tfg.productionservice.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.productionservice.service.ProductionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProductionConsumer {

    private final ProductionService productionService;

    public ProductionConsumer(ProductionService productionService) {
        this.productionService = productionService;
    }

    @RabbitListener(queues = ProductionPublish.PRODUCTION_QUEUE)
    public void consume(JsonNode event) {
        System.out.println(">>> MENSAJE RECIBIDO: " + event.toString());

        String eventType = event.path("eventType").asText();
        Long productionId = event.path("productionId").asLong();
        int amount = event.path("amount").asInt();

        System.out.println(">>> eventType: " + eventType);
        System.out.println(">>> productionId: " + productionId);

        if ("production.approved".equals(eventType)) {
            productionService.startProduction(productionId);
        }
        if ("production.rejected".equals(eventType)) {
            productionService.compensateRejectedProduction(productionId, amount);
        }
    }
}
