package com.tfg.ledgerservice.message;

import com.tfg.ledgerservice.event.LedgerEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class LedgerPublish {

    // Define queue for sending Inventory messages
    public static final String INVENTORY_QUEUE = "inventory.queue";
    // Define queue for receiving Inventory messages
    public static final String DELIVERY_QUEUE = "delivery.queue";
    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    public LedgerPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Define a queue for Inventory to receive events
    @Bean
    public Queue inventoryQueue() {
        return new Queue(INVENTORY_QUEUE, true);
    }

    // Define a queue for Production to receive events
    @Bean
    public Queue deliveryQueue() {
        return new Queue(DELIVERY_QUEUE, true);
    }

    public void publishLedgerMovementCreated(Long deliveryId, int amount) {
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE,
                       new LedgerEvent("delivery.created", deliveryId, null, amount));
    }

    public void publishLedgerMovementCompleted(Long deliveryId, int amount) {
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE,
                       new LedgerEvent("delivery.completed", deliveryId, null, amount));
    }
    
    // Given an ID of delivery and the amount of delivery 
    // publishes an event to Inventory to release the stock reservation
    public void publishReservationRelease(Long deliveryId, int amount) {
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE,
                new LedgerEvent("delivery.reservation.release", deliveryId, null, amount));
    }
    
    // Given an Id and amount of delivery publish a cancelled delivery
    public void publishLedgerMovementCancelled(Long deliveryId, Long productionId, int amount) {
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE,
                  new LedgerEvent("delivery.cancelled", deliveryId, productionId, amount));
    }
        
    // Given an Id and amount of delivery publish a pending delivery
    public void publishLedgerMovementPending(Long deliveryId, int amount) {
        LedgerEvent deliveryEvent = new LedgerEvent(
                                      "delivery.pending", deliveryId, null, amount);
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, deliveryEvent);
    }

}
