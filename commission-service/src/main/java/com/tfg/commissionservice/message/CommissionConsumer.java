package com.tfg.commissionservice.message;

import com.tfg.commissionservice.event.CommissionEvent;
import com.tfg.commissionservice.model.CommissionMethod;
import com.tfg.commissionservice.service.CommissionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class CommissionConsumer {
    // Constant
    private final CommissionService commissionService;

    // Constructor
    public CommissionConsumer(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @RabbitListener(queues = CommissionPublish.COMMISSION_QUEUE)
    private void processEvent(CommissionEvent event) {
        if (event == null) {
            return;
        }

        // Get parameters to use in switch cases
        Long idReceived = event.getPaymentId();
        String correlationId = event.getCorrelationId();
        BigDecimal amount = event.getTotalAmount();
        String method = event.getMethod();
        String eventType = event.getEventType();

        switch (eventType) {
            case "payment.created":
                commissionService.calculateCommission(idReceived, correlationId, amount, method);
                break;
            case "balance.rejected":
                commissionService.commissionRelease(idReceived, correlationId);
                break;

            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
}
