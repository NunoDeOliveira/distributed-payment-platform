package com.tfg.paymentservice.message;

import com.tfg.paymentservice.event.PaymentEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Component
public class PaymentPublish {
    // Define queue where the Payment send messages
    public static final String INVENTORY_QUEUE = "inventory.queue";
    // Define queue where the Payment receive messages
    public static final String PRODUCTION_QUEUE = "payment.queue";

    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    public PaymentPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Define a queue for Inventory to receive events
    @Bean
    public Queue inventoryQueue() {
        return new Queue(INVENTORY_QUEUE, true);
    }

    // Define a queue for Payment to receive events
    @Bean
    public Queue paymentQueue() {
        return new Queue(PRODUCTION_QUEUE, true);
    }
    
    // Payment publish payment created
    public void publishPaymentCreated(Long paymentId, int amount) {
        PaymentEvent event = new PaymentEvent(
                                "payment.created", paymentId, amount);
        // publish payment created                       
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, event);
    }

    public void publishPaymentCompleted(Long paymentId, int amount) {
        PaymentEvent event = new PaymentEvent(
                                "payment.completed", paymentId, amount);
        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, event);
    }
    
    public void publishPaymentCancelled(Long paymentId, int amount) {
        PaymentEvent event = new PaymentEvent(
                            "payment.cancelled", paymentId, amount);
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, event);
    }
    
    /*
    public void publishPaymentPending(Long paymentId, int amount) {
        PaymentEvent paymentEvent = new PaymentEvent(
                                        "payment.pending", paymentId, amount);
        rabbitTemplate.convertAndSend(INVENTORY_QUEUE, paymentEvent);
    }*/
    
}
