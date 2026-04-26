package com.tfg.inventoryservice.message;

import com.tfg.inventoryservice.event.InventoryEvent;
import com.tfg.inventoryservice.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private final InventoryService inventoryService;

    public InventoryConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // This method receive two type of events to consume.
    //
    @RabbitListener(queues = InventoryPublish.INVENTORY_QUEUE)
    public void consume(InventoryEvent event) {

        if ("production.requested".equals(event.getEventType())) {
            inventoryService.validateProduction(
                    event.getProductionId(), event.getAmount());
        }

        else if ("production.completed".equals(event.getEventType())) {
            inventoryService.increaseStock(
                    event.getProductionId(), event.getAmount());
        }
    }
}
