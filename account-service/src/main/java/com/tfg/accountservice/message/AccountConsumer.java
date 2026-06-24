package com.tfg.accountservice.message;

import com.tfg.accountservice.service.AccountService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AccountConsumer {

    private final AccountService inventoryService;

    public AccountConsumer(AccountService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = AccountPublish.INVENTORY_QUEUE)
    public void consume(JsonNode event) {
        String eventType = event.path("eventType").asText();
        int amount = event.path("amount").asInt();
        
        Long productionId = null;
        Long deliveryId = null;
        if (eventType.startsWith("production")) {
            productionId = event.path("productionId").asLong();
        } else {
            deliveryId = event.path("deliveryId").asLong();
            if (eventType.equals("delivery.cancelled")) {
                productionId = event.path("productionId").asLong();
            }
        }

        System.out.println("Account receive: " + eventType +
                " productionId=" + productionId + " deliveryId=" + deliveryId);

        switch (eventType) {
            case "production.created":
                inventoryService.validateProduction(productionId, amount);
                break;
            case "production.completed":
                inventoryService.increaseStock(productionId, amount);
                break;
            case "delivery.created":
                inventoryService.reserveDeliveryStock(deliveryId, amount);
                break;
            case "delivery.completed":
                inventoryService.confirmDelivery(deliveryId, amount);
                break;
            // Case for productions pendings
            //case "production.pending":
                //inventoryService.validateProduction(productionId, amount);
                //break;
            case "delivery.cancelled":
                inventoryService.cancelDelivery(deliveryId, productionId, amount); 
                break;
            case "production.cancelled":
                inventoryService.cancelProduction(productionId, amount);
                break;
            // Compensating Transaction
            case "delivery.reservation.release":
                inventoryService.releaseReservedStock(deliveryId, amount);
                break;
            //case "delivery.pending":
                //inventoryService.validateDelivery(deliveryId, amount);
                //break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
}
