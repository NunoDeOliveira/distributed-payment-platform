package com.tfg.paymentservice.message;

import com.tfg.paymentservice.event.PaymentEvent;
import com.tfg.paymentservice.service.PaymentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PaymentConsumer {

    private final PaymentService paymentService;
    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);


    public PaymentConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Listens on payment.queue for events published by other services
    @RabbitListener(queues = PaymentPublish.PAYMENT_QUEUE)
    public void processEvent(PaymentEvent event) {
        if (event == null) {
            System.out.println("Received null or invalid event");
            return;
        }

        // Manage all the possible states
        String eventType = event.getEventType();
        switch (eventType) {
            // Case in which payment is finished successful
            case "amount.deducted":
                paymentService.completePayment(event.getCorrelationId());
                break;
            case "operation.rejected":
                paymentService.releasePayment(event.getCorrelationId());
                break;
            case "operation.canceled":
                paymentService.cancellationReceived(event.getCorrelationId(), event.getAmount());
                log.info("RECEIVED CANCEL EVENT | eventType={} | correlationId={} | amount={}",
                        event.getEventType(), event.getCorrelationId(), event.getAmount());
                break;

            default:
                System.out.println("Event unknown: " + eventType);
        }
    }
        
}
