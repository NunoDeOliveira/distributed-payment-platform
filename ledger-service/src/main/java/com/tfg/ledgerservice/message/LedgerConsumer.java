package com.tfg.ledgerservice.message;

import com.tfg.ledgerservice.event.LedgerEvent;
import org.springframework.stereotype.Component;
import com.tfg.ledgerservice.service.LedgerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


@Component
public class LedgerConsumer {

    private final LedgerService ledgerService;

    public LedgerConsumer(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @RabbitListener(queues = LedgerPublish.LEDGER_QUEUE)
    private void processEvent(LedgerEvent event) {

        String eventType = event.getEventType();
        switch (eventType) {
            case "hold.funds":
                ledgerService.recordMovement(event.getLedgerId(), event.getCorrelationId(), event.getAmount());
                break;
            case "hold.founds.canceled":
                ledgerService.cancelMovement(event.getLedgerId(),event.getCorrelationId());
                break;

            default:
                System.out.println("Event unknown: " + eventType);
        }
    }


}
