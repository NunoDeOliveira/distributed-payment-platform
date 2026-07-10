package com.tfg.accountservice.message;

import com.tfg.accountservice.event.AccountEvent;
import com.tfg.accountservice.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AccountConsumer {
    // Attributes
    private final AccountService accountService;
    private static final Logger log = LoggerFactory.getLogger(AccountConsumer.class);

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
                accountService.reserveAmount(event.getCorrelationId(), event.getAmount());
                log.info("CONSUMER | commission.calculated | correlationId={} | amount={}",
                        event.getCorrelationId(), event.getAmount());
                break;
            case "movement.recorded":
                accountService.deductAmount(event.getCorrelationId(), event.getAmount());
                log.info("CONSUMER | movement.recorded | correlationId={} | amount={}",
                        event.getCorrelationId(), event.getAmount());
                break;
            case "operation.canceled":
                accountService.cancelReserveAmount(event.getCorrelationId(), event.getAmount());
                log.info("CONSUMER | operation.canceled | correlationId={} | amount={}",
                        event.getCorrelationId(), event.getAmount());
                break;

            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
}
