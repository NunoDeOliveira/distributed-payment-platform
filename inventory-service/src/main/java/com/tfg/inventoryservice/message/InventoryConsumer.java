package com.tfg.inventoryservice.message;

import com.tfg.inventoryservice.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class InventoryConsumer {

    private final InventoryService inventoryService;

    public InventoryConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = InventoryPublish.INVENTORY_QUEUE)
    public void consume(JsonNode event) {
        String eventType = event.path("event").asText();
        Long productionId = event.path("productionId").asLong();
        int amount = event.path("amount").asInt();

        if ("production.created".equals(eventType)) {
            inventoryService.validateProduction(productionId, amount);
        } else if ("production.completed".equals(eventType)) {
            inventoryService.increaseStock(productionId, amount);
        }
    }
}
