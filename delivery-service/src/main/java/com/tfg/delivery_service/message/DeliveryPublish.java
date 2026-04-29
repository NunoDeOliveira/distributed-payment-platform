package com.tfg.delivery_service.message;

import com.tfg.delivery_service.event.DeliveryEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DeliveryPublish {

    // Define queue for sending Inventory messages
    public static final String INVENTORY_QUEUE = "inventory.queue";
    // Define queue for receiving Inventory messages
    public static final String DELIVERY_QUEUE = "delivery.queue";

    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    public DeliveryPublish(RabbitTemplate rabbitTemplate) {
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
        return new Queue(DELIVERY_QUEUE, true);
    }

    public void publishDeliveryCreated(Long deliveryId, int amount) {
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE,
                new DeliveryEvent("delivery.created", deliveryId, amount));
    }

    public void publishDeliveryCompleted(Long deliveryId, int amount) {
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE,
                new DeliveryEvent("delivery.completed", deliveryId, amount));
    }

}
