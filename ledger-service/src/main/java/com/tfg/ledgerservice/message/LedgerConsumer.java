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
        if (event == null) return;

        switch (event.getEventType()) {
            case "amount.reserved":
                ledgerService.recordMovement(event.getCorrelationId(), event.getAmount());
                log.info("CONSUMER | amount.reserved | correlationId={}", event.getCorrelationId());
                break;
            case "deposit.created":
                ledgerService.recordDeposit(event.getCorrelationId(), event.getAmount());
                log.info("CONSUMER | deposit.created | correlationId={}", event.getCorrelationId());
                break;
            default:
                log.warn("CONSUMER | unknown event | eventType={}", event.getEventType());
        }
    }

    @RabbitListener(queues = LedgerPublish.LEDGER_CANCEL_QUEUE)
    public void processCanceledEvent(LedgerEvent event) {
        if (!"operation.canceled".equals(event.getEventType())) {
            return;
        }
        ledgerService.cancelMovement(event.getCorrelationId());
        log.info("CONSUMER | operation.canceled | correlationId={}", event.getCorrelationId());
    }


}
