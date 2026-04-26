package com.tfg.productionservice.message;

import com.tfg.productionservice.event.ProductionEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ProductionPublish {
    // Define queue where the Production send messages
    public static final String INVENTORY_QUEUE = "inventory.queue";
    // Define queue where the Production receive messages
    public static final String PRODUCTION_QUEUE = "production.queue";

    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    public ProductionPublish(RabbitTemplate rabbitTemplate) {

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

    public void publishProductionCreated(Long productionId, int amount) {
        ProductionEvent event = new ProductionEvent(
                "production.created",
                productionId,
                amount
        );

        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, event);
    }

    public void publishProductionCompleted(Long productionId, int amount) {
        ProductionEvent event = new ProductionEvent(
                "production.completed",
                productionId,
                amount
        );

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, event);
    }
}