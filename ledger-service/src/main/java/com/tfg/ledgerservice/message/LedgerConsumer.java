package com.tfg.ledgerservice.message;

import com.tfg.ledgerservice.event.LedgerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.tfg.ledgerservice.service.LedgerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


@Component
public class LedgerConsumer {

    private final LedgerService ledgerService;
    private static final Logger log = LoggerFactory.getLogger(LedgerConsumer.class);


    public LedgerConsumer(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @RabbitListener(queues = LedgerPublish.LEDGER_QUEUE)
    public void processEvent(LedgerEvent event) {

        String eventType = event.getEventType();
        switch (eventType) {
            case "amount.reserved":
                ledgerService.recordMovement(event.getCorrelationId(), event.getAmount());
                log.info("CONSUMER | amount.reserved | correlationId={} | totalAmount={}",
                        event.getCorrelationId(), event.getAmount());
                break;
            case "operation.canceled":
                ledgerService.cancelMovement(event.getCorrelationId());
                System.out.println("operation.canceled: " + event.getEventType()
                                + " correlationId=" + event.getCorrelationId()
                                + " amount=" + event.getAmount());
                break;

            default:
                System.out.println("Event unknown: " + eventType);
        }
    }


}
