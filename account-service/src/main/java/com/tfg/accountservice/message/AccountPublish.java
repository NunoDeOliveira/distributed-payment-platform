package com.tfg.accountservice.message;

import com.tfg.accountservice.event.AccountEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Component
public class AccountPublish {
    // Define queue where the account send messages
    public static final String ACCOUNT_QUEUE = "account.queue";
    // Define queue where the commission receive messages
    public static final String COMMISSION_QUEUE = "commission.queue";
    // Define queue where the ledge send messages
    public static final String LEDGER_QUEUE = "ledger.queue";
    // Define queue where the ledge send messages
    public static final String PAYMENT_QUEUE = "payment.queue";
    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(AccountPublish.class);

    // Constructor
    public AccountPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public Queue accountQueue() {
        return new Queue(ACCOUNT_QUEUE, true);
    }

    @Bean
    public Queue commissionQueue() {
        return new Queue(COMMISSION_QUEUE, true);
    }

    @Bean
    public Queue ledgeQueue() {
        return new Queue(LEDGER_QUEUE, true);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }


    // Publish an event accepting delivery in the delivery queue
    public void publishAmountRejected(String correlationId) {
        // Create an object account event to publish in commission queue
        AccountEvent event;
        event = new AccountEvent("amount.rejected", correlationId, null, null);

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(COMMISSION_QUEUE, event);
    }

    // Publish an event in Ledge queue
    public void publishAmountReserved(String correlationId, BigDecimal totalAmount) {
        // Create an object account event to publish in the ledge queue
        AccountEvent event;
        event = new AccountEvent("amount.reserved", correlationId, totalAmount, null);

        log.info("PUBLISH | amount.reserved | correlationId={} | amount={}",
                correlationId, totalAmount);

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(LEDGER_QUEUE, event);
    }

    //
    public void publishAmountDeducted(String correlationId, BigDecimal totalAmount) {
        // Create an object account event to publish in commission queue
        AccountEvent event;
        event = new AccountEvent("amount.deducted", correlationId, totalAmount,null);

        log.info("PUBLISH | amount.debited | correlationId={} | amount={}",
                correlationId, totalAmount);

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(PAYMENT_QUEUE, event);
    }

    public void publishAmountReleased(String correlationId) {
        // Create an object account event to publish in commission queue
        AccountEvent event;
        event = new AccountEvent("amount.released", correlationId, null, null);

        log.info("PUBLISH | amount.released | correlationId={} | amount={}",
                correlationId, event.getAmount());

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(COMMISSION_QUEUE, event);
    }

    public void publishAmountCanceled(String correlationId, BigDecimal amount) {
        // Create an object account event to publish in commission queue
        AccountEvent event;
        event = new AccountEvent("operation.canceled", correlationId, amount, null);

        log.info("PUBLISH | operation.canceled | correlationId={} | amount={}",
                correlationId, event.getAmount());

        // Convert to JSON format and send
        rabbitTemplate.convertAndSend(COMMISSION_QUEUE, event);
    }



}
