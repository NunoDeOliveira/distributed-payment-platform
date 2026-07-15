package com.tfg.commissionservice.message;

import com.tfg.commissionservice.event.CommissionEvent;
import com.tfg.commissionservice.model.Commission;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Component
public class CommissionPublish {

    public static final String COMMISSION_QUEUE = "commission.queue";
    public static final String ACCOUNT_QUEUE = "account.queue";
    public static final String PAYMENT_QUEUE = "payment.queue";

    private final RabbitTemplate rabbitTemplate;

    public CommissionPublish(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public Queue commissionQueue() {
        return new Queue(COMMISSION_QUEUE, true);
    }

    @Bean
    public Queue accountQueue() {
        return new Queue(ACCOUNT_QUEUE, true);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }


    // Given a commission publish commission calculated
    public void publishCommissionCalculated(String correlationId, BigDecimal amount,
                                            BigDecimal totalAmount, String method) {

        CommissionEvent event = new CommissionEvent("commission.calculated",
                                                    correlationId, totalAmount, totalAmount, method);
        // Send to rabbit account queue
        rabbitTemplate.convertAndSend(ACCOUNT_QUEUE, event);
    }

    // Given a commission publish commission released
    public void publishCommissionRejected(String correlationId) {
        CommissionEvent event;
        event = new CommissionEvent("operation.rejected", correlationId, null, null, null);

        // Send to rabbit payment queue
        rabbitTemplate.convertAndSend(PAYMENT_QUEUE, event);
    }

    // Given a commission publish commission released
    public void publishCommissionCanceled(String correlationId, BigDecimal totalAmount) {
        CommissionEvent event;
        event = new CommissionEvent("operation.canceled", correlationId, null, totalAmount, null);

        // Send to rabbit payment queue
        rabbitTemplate.convertAndSend(PAYMENT_QUEUE, event);
    }



}