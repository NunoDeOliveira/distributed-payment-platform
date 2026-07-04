package com.tfg.accountservice.message;

import com.tfg.accountservice.event.AccountEvent;
import com.tfg.accountservice.service.AccountService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AccountConsumer {
    // Attributes
    private final AccountService accountService;

    // Constructor
    public AccountConsumer(AccountService accountService) {
        this.accountService = accountService;
    }


    @RabbitListener(queues = AccountPublish.ACCOUNT_QUEUE)
    public void processEvent(AccountEvent event) {
        if (event == null) {
            return;
        }

        // Manage all the possible states
        String eventType = event.getEventType();
        switch (eventType) {
            case "commission.calculated":
                accountService.holdFunds(event.getAccountId(), event.getCorrelationId(), event.getAmount());
                break;
            case "movement.recorded":
                accountService.deductAccount(event.getAccountId(), event.getCorrelationId());
                break;
            case "payment.cancelled":
                accountService.cancelOperation(event.getAccountId(), event.getCorrelationId());
                break;

            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
}
