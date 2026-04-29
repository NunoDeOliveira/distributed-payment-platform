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
        int amount = event.path("amount").asInt();
        
        Long productionId = null;
        Long deliveryId = null;
        if (eventType.startsWith("production")) {
            productionId = event.path("productionId").asLong();
        } else {
            deliveryId = event.path("deliveryId").asLong();
        }

        System.out.println("Inventory receive: " + eventType +
                " productionId=" + productionId + " deliveryId=" + deliveryId);

        switch (eventType) {
            case "production.created":
                inventoryService.validateProduction(productionId, amount);
                break;
            case "production.completed":
                inventoryService.increaseStock(productionId, amount);
                break;
            case "delivery.created":
                inventoryService.validateDelivery(deliveryId, amount);
                break;
            case "delivery.completed":
                inventoryService.confirmDelivery(deliveryId, amount);
                break;
            // Compensating Transaction
            case "delivery.rejected":
                inventoryService.releaseReservedStock(deliveryId, amount);
                break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
}
