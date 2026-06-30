package com.tfg.paymentservice.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.paymentservice.service.PaymentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import com.tfg.paymentservice.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private final PaymentService paymentService;

    public PaymentConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @RabbitListener(queues = PaymentPublish.PRODUCTION_QUEUE)
    public void consume(JsonNode event, 
        @Header(value = "x-delivery-count", defaultValue = "0") int retryCount) {
        //System.out.println("Message received: " + event.toString());
        
        // Extract data
        String eventType = event.path("eventType").asText();
        Long paymentId = event.path("paymentId").asLong();
        int amountAllowed = event.path("amount").asInt();   // this is the amount allowed
        System.out.println("EventType: " + eventType + "PaymentId: " + paymentId);

        try {
            // procecess event received
            processEvent(eventType, paymentId, amountAllowed);
        } catch (Exception e) {
            ///// Manage timeout or failed
            if (paymentId != 0) {
                if (retryCount >= 2) {
                    // third trying release
                    paymentService.getTimeoutState(paymentId); 
                }
            }
            throw e;
        }       
    }
    
    // Process the event given. Case aproved or case rejected
    private void processEvent(String eventType, Long paymentId, int amountAllowed) {
        switch (eventType) {
            case "payment.accepted":
                Payment payment = paymentService.getPayment(paymentId);
                paymentService.startPayment(paymentId);
                break;
            case "payment.rejected":
                Payment paymentRejected = paymentService.getPayment(paymentId);
                paymentService.rejectPayment(paymentRejected, amountAllowed);
                break;
            case "payment.cancelled":
                paymentService.cancelPayment(paymentId);
                break;
            //case "stock.available":
                //paymentService.processPendingPayments();
                //break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
        
}
