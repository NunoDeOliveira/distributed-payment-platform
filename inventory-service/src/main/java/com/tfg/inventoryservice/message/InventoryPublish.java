package com.tfg.inventoryservice.message;

import com.tfg.inventoryservice.event.InventoryEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InventoryPublish {
    // Define queue where the Production send messages
    public static final String INVENTORY_QUEUE = "inventory.queue";
    // Define queue where the Production receive messages
    public static final String PRODUCTION_QUEUE = "production.queue";
    // Define queue where the Delivery send messages
    public static final String DELIVERY_QUEUE = "delivery.queue";

    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    public InventoryPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Define a queue for Inventory to receive events
    @Bean
    public Queue inventoryQueue() {
        return new Queue(INVENTORY_QUEUE, true);
    }

    // Define a queue for Production to receive events
    @Bean
    public Queue productionQueue() {
        return new Queue(PRODUCTION_QUEUE, true);
    }

    @Bean
    public Queue deliveryQueue() {
        return new Queue(DELIVERY_QUEUE, true);
    }

    // Publish an event accepting production in the Production queue
    public void publishProductionAccepted(Long productionId, int amount) {
        InventoryEvent inventoryEvent = new InventoryEvent(
                "production.accepted", productionId, amount);

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(PRODUCTION_QUEUE, inventoryEvent);
    }

    // Publish an event rejecting production in the Production queue
    public void publishProductionRejected(Long productionId, int amount) {
        InventoryEvent inventoryEvent = new InventoryEvent(
                "production.rejected", productionId, amount);

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(PRODUCTION_QUEUE, inventoryEvent);
    }

    // Publish an event accepting delivery in the delivery queue
    public void publishDeliveryAccepted(Long deliveryId, int amount) {
        InventoryEvent inventoryEvent = new InventoryEvent(
                "delivery.accepted", deliveryId, amount);

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(DELIVERY_QUEUE, inventoryEvent);
    }

    // Publish an event rejecting production in the Production queue
    public void publishDeliveryRejected(Long deliveryId, int amount) {
        InventoryEvent inventoryEvent = new InventoryEvent(
                "delivery.rejected", deliveryId, amount);

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(DELIVERY_QUEUE, inventoryEvent);
    }

}
