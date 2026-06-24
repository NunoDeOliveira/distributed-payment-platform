package com.tfg.transferservice.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.transferservice.service.TransferService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import com.tfg.transferservice.model.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferConsumer {

    private final TransferService productionService;

    public TransferConsumer(TransferService productionService) {
        this.productionService = productionService;
    }
    
    @RabbitListener(queues = TransferPublish.PRODUCTION_QUEUE)
    public void consume(JsonNode event, 
        @Header(value = "x-delivery-count", defaultValue = "0") int retryCount) {
        //System.out.println("Message received: " + event.toString());
        
        // Extract data
        String eventType = event.path("eventType").asText();
        Long productionId = event.path("productionId").asLong();
        int amountAllowed = event.path("amount").asInt();   // this is the amount allowed
        System.out.println("EventType: " + eventType + "TransferId: " + productionId);

        try {
            // procecess event received
            processEvent(eventType, productionId, amountAllowed);
        } catch (Exception e) {
            ///// Manage timeout or failed
            if (productionId != 0) {
                if (retryCount >= 2) {
                    // third trying release
                    productionService.getTimeoutState(productionId); 
                }
            }
            throw e;
        }       
    }
    
    // Process the event given. Case aproved or case rejected
    private void processEvent(String eventType, Long productionId, int amountAllowed) {
        switch (eventType) {
            case "production.accepted":
                Transfer production = productionService.getTransfer(productionId);
                productionService.startTransfer(productionId);
                break;
            case "production.rejected":
                Transfer productionRejected = productionService.getTransfer(productionId);
                productionService.rejectTransfer(productionRejected, amountAllowed);
                break;
            case "production.cancelled":
                productionService.cancelTransfer(productionId);
                break;
            //case "stock.available":
                //productionService.processPendingTransfers();
                //break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
        
}
