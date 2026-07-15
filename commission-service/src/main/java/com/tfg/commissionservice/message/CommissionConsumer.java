package com.tfg.commissionservice.message;

import com.tfg.commissionservice.event.CommissionEvent;
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
    public void processEvent(CommissionEvent event) {
        if (event == null) {
            return;
        }

        String eventType = event.getEventType();
        switch (eventType) {
            case "payment.created":
                commissionService.calculateCommission(event.getCorrelationId(),
                                                    event.getAmount(), event.getMethod());
                break;
            case "amount.rejected":
                commissionService.rejectedCommission(event.getCorrelationId());
                break;
            case "operation.canceled":
                commissionService.cancelCommission(event.getCorrelationId());
                break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
}
