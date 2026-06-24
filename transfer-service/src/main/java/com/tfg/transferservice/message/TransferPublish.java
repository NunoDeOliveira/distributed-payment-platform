package com.tfg.transferservice.message;

import com.tfg.transferservice.event.TransferEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Component
public class TransferPublish {
    // Define queue where the Transfer send messages
    public static final String INVENTORY_QUEUE = "inventory.queue";
    // Define queue where the Transfer receive messages
    public static final String PRODUCTION_QUEUE = "production.queue";

    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    public TransferPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Define a queue for Inventory to receive events
    @Bean
    public Queue inventoryQueue() {
        return new Queue(INVENTORY_QUEUE, true);
    }

    // Define a queue for Transfer to receive events
    @Bean
    public Queue productionQueue() {
        return new Queue(PRODUCTION_QUEUE, true);
    }
    
    // Transfer publish production created
    public void publishTransferCreated(Long productionId, int amount) {
        TransferEvent event = new TransferEvent(
                                "production.created", productionId, amount);
        // publish production created                       
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, event);
    }

    public void publishTransferCompleted(Long productionId, int amount) {
        TransferEvent event = new TransferEvent(
                                "production.completed", productionId, amount);
        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, event);
    }
    
    public void publishTransferCancelled(Long productionId, int amount) {
        TransferEvent event = new TransferEvent(
                            "production.cancelled", productionId, amount);
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, event);
    }
    
    /*
    public void publishTransferPending(Long productionId, int amount) {
        TransferEvent productionEvent = new TransferEvent(
                                        "production.pending", productionId, amount);
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, productionEvent);
    }*/
    
}
