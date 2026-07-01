package com.tfg.paymentservice.message;

import com.tfg.paymentservice.event.PaymentEvent;
import com.tfg.paymentservice.model.PaymentMethod;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PaymentPublish {
    // Define queue where the Payment send messages
    public static final String COMMISSION_QUEUE = "inventory.queue";
    // Define queue where the Payment receive messages
    public static final String PAYMENT_QUEUE = "payment.queue";
    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;


    // Constructor
    public PaymentPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Define a queue for Inventory to receive events
    @Bean
    public Queue commissionQueue() {
        return new Queue(COMMISSION_QUEUE, true);
    }

    // Define a queue for Payment to receive events
    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }


    // Payment publish payment created
    public void publishPaymentCreated(Long paymentId, String CorrelationId, BigDecimal amount, PaymentMethod method) {
        PaymentEvent event = new PaymentEvent(
                "payment.created", paymentId, CorrelationId, amount, method, LocalDateTime.now());
        // publish payment created                       
        rabbitTemplate.convertAndSend(COMMISSION_QUEUE, event);
    }
    
    public void publishPaymentCancelled(Long paymentId, String correlationId) {
        PaymentEvent event = new PaymentEvent("payment.cancelled", paymentId, correlationId,null, null, LocalDateTime.now());
        rabbitTemplate.convertAndSend(COMMISSION_QUEUE, event);
    }

    
}
