package com.tfg.paymentservice.event;

import com.tfg.paymentservice.model.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentEvent {

    private String eventType; // Type: created, completed, canceled, rejected, failed
    private String correlationId;
    private BigDecimal amount;
    private PaymentMethod method;

    public PaymentEvent() {
    }

    public PaymentEvent(String eventType, String correlationId, BigDecimal amount, PaymentMethod method) {

        this.eventType = eventType;
        this.correlationId = correlationId;
        this.amount = amount;
        this.method = method;
    }

}
