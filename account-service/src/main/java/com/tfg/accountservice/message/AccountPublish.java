package com.tfg.accountservice.message;

import com.tfg.accountservice.event.AccountEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Component
public class AccountPublish {
    // Define queue where the Production send messages
    public static final String INVENTORY_QUEUE = "inventory.queue";
    // Define queue where the Production receive messages
    public static final String PRODUCTION_QUEUE = "production.queue";
    // Define queue where the Delivery send messages
    public static final String DELIVERY_QUEUE = "delivery.queue";

    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    public AccountPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Define a queue for Account to receive events
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
        AccountEvent inventoryEvent = new AccountEvent(
                                            "production.accepted", productionId, null,amount);
        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(PRODUCTION_QUEUE, inventoryEvent);
    }

    // Publish an event rejecting production in the Production queue
    public void publishProductionRejected(Long productionId, int amount) {
        AccountEvent inventoryEvent = new AccountEvent(
                                            "production.rejected", productionId, null, amount);
        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(PRODUCTION_QUEUE, inventoryEvent);
    }

    // Publish an event accepting delivery in the delivery queue
    public void publishDeliveryAccepted(Long deliveryId, int amount) {
        AccountEvent inventoryEvent = new AccountEvent(
                                            "delivery.accepted", null, deliveryId, amount);
        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(DELIVERY_QUEUE, inventoryEvent);
    }

    // Publish an event rejecting production in the Production queue
    public void publishDeliveryRejected(Long deliveryId, int amount) {
        AccountEvent inventoryEvent = new AccountEvent(
                                            "delivery.rejected", null, deliveryId, amount);
        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(DELIVERY_QUEUE, inventoryEvent);
    }
    
    // Notify production cancellation 
    public void publishProductionCancelled(Long productionId, int amount) {
        AccountEvent event = new AccountEvent(
                                    "production.cancelled", productionId, null, amount);
        rabbitTemplate.convertAndSend(PRODUCTION_QUEUE, event);
    }
    
    ///////////////////
    public void publishDeliveryCancelledByProduction(Long productionId, int amount) {
        AccountEvent event = new AccountEvent(
                                "delivery.cancel", null, null, amount);
        event.setProductionId(productionId);
        rabbitTemplate.convertAndSend(DELIVERY_QUEUE, event);
    }

    // Notify delivery service that stock is available
    public void publishStockAvailable(Long productionId, int amount) {
        AccountEvent inventoryEvent = new AccountEvent( 
                                      "stock.available", productionId, null, amount);
        rabbitTemplate.convertAndSend(DELIVERY_QUEUE, inventoryEvent);
    }
    
    // Notify payment-service that space has been release
    public void publishCapacityAvailable(int amount) {
        AccountEvent inventoryEvent = new AccountEvent(
                                            "capacity.available", null, null, amount);
        rabbitTemplate.convertAndSend(PRODUCTION_QUEUE, inventoryEvent);
    }
    
    // Given an amount publish 
    public void publishForCreateDelivery(int amount) {
        AccountEvent inventoryEvent = new AccountEvent("create.delivery",
                                                            null, null, amount);

        rabbitTemplate.convertAndSend(DELIVERY_QUEUE, inventoryEvent);
    }
    

}
