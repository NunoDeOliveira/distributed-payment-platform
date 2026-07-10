package com.tfg.paymentservice.message;

import com.tfg.paymentservice.event.PaymentEvent;
import com.tfg.paymentservice.model.PaymentMethod;
import com.tfg.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final String COMMISSION_QUEUE = "commission.queue";
    // Define queue where the Payment receive messages
    public static final String PAYMENT_QUEUE = "payment.queue";
    // define the queue where the payment publish to ledger service
    public static final String LEDGER_CANCEL_QUEUE = "ledger.cancel.queue";
    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);


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

    // Define a queue for Ledger to receive events
    @Bean
    public Queue ledgerQueue() {
        return new Queue(LEDGER_CANCEL_QUEUE, true);
    }


    // Payment publish payment created
    public void publishPaymentCreated(Long paymentId, String CorrelationId, BigDecimal amount, PaymentMethod method) {
        PaymentEvent event = new PaymentEvent(
                "payment.created", CorrelationId, amount, method);
        // publish payment created                       
        rabbitTemplate.convertAndSend(COMMISSION_QUEUE, event);
    }
    
    public void publishPaymentCancelled(Long paymentId, String correlationId, BigDecimal amount) {
        PaymentEvent event = new PaymentEvent("operation.canceled",
                                                correlationId, amount, null);

        log.info("PUBLISH | operation.canceled | queue={} | correlationId={} | amount={}",
                LEDGER_CANCEL_QUEUE, correlationId, event.getAmount());
        rabbitTemplate.convertAndSend(LEDGER_CANCEL_QUEUE, event);
    }

    
}
