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
    public void publishCommissionCalculated(Long Id, String CorrelationId, BigDecimal totalAmount, String method) {
        CommissionEvent event = new CommissionEvent(
                                        "commission.calculated", Id, CorrelationId, totalAmount, method);

        // Send to rabbit account queue
        rabbitTemplate.convertAndSend(ACCOUNT_QUEUE, event);
    }

    public void publishCommissionReleased(Commission commission) {
        CommissionEvent event;
        event = new CommissionEvent("commission.released",
                                                commission.getOperationId(),
                                                commission.getCorrelationId(),
                                                null, null);

        // Send to rabbit payment queue
        rabbitTemplate.convertAndSend(PAYMENT_QUEUE, event);
    }

    /*public void publishCommissionRejected(Commission commission) {
        CommissionEvent event = new CommissionEvent("commission.rejected",
                                                    commission.getId(),
                                                    commission.getCorrelationId(),
                                                    commission.getAmount(),
                                                    commission.getCommissionAmount(),
                                                    commission.getTotalAmount(),
                                                    commission.getMethod(), LocalDateTime.now());

        rabbitTemplate.convertAndSend(PAYMENT_QUEUE, event);
    }*/
}