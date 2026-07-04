package com.tfg.ledgerservice.message;

import com.tfg.ledgerservice.event.LedgerEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LedgerPublish {

    // Define queue for sending account messages
    public static final String ACCOUNT_QUEUE = "account.queue";
    // Define queue for receiving account messages
    public static final String LEDGER_QUEUE = "ledger.queue";
    // The variable to use the RabbitTemplate class
    private final RabbitTemplate rabbitTemplate;

    public LedgerPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public Queue accountQueue() {
        return new Queue(ACCOUNT_QUEUE, true);
    }

    @Bean
    public Queue ledgerQueue() {
        return new Queue(LEDGER_QUEUE, true);
    }


    // Methods
    public void publishLedgerMovementRecorded(Long ledgerId, String correlationId, BigDecimal amount) {
        // Create an event to publish in the queue
        LedgerEvent event =  new LedgerEvent("movement.recorded",  ledgerId, correlationId, amount);

        rabbitTemplate.convertAndSend(ACCOUNT_QUEUE, event);
    }
        
    // Given an Id and amount of movement publish movement rejected
    public void publishLedgerMovementRejected(Long ledgerId, String correlationId, BigDecimal amount) {
        // Create an event to publish in the queue
        LedgerEvent event =  new LedgerEvent("movement.rejected",  ledgerId, correlationId, amount);

        rabbitTemplate.convertAndSend(ACCOUNT_QUEUE, event);
    }



    // Given an Id and amount of delivery publish a pending delivery
    public void publishLedgerMovementFailed(Long ledgerId, String correlationId, BigDecimal amount) {
        // Create an event to publish in the queue
        LedgerEvent event =  new LedgerEvent("movement.failed",  ledgerId, correlationId, amount);

        rabbitTemplate.convertAndSend(ACCOUNT_QUEUE, event);
    }

}
