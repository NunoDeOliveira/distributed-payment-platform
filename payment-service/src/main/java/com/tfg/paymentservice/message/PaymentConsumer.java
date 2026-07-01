package com.tfg.paymentservice.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.paymentservice.event.PaymentEvent;
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

    // Listens on payment.queue for events published by other services
    @RabbitListener(queues = PaymentPublish.PAYMENT_QUEUE)
    private void processEvent(PaymentEvent event) {
        if (event == null) {
            System.out.println("Received null or invalid event");
            return;
        }

        // Manage all the possible states
        String eventType = event.getEventType();
        switch (eventType) {
            // Case in which payment is finished successful
            case "movement.recorded":
                paymentService.completedPayment(event.getPaymentId(), event.getCorrelationId());
                break;
            // Case in which payment is canceled by a user
            /*case "payment.cancelled":
                paymentService.cancelPayment(event.getPaymentId(), event.getCorrelationId());
                break;*/
            // Case in which a payment is rejected due to insufficient balance
            case "payment.rejected":
                paymentService.rejectPayment(event.getPaymentId(), event.getCorrelationId());
                break;
            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
        
}
