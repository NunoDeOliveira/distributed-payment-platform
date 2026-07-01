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
    private Long paymentId;
    private String correlationId;
    private BigDecimal amount;
    private PaymentMethod method;
    private LocalDateTime occurredAt;

    public PaymentEvent() {
    }

    public PaymentEvent(String eventType, Long paymentId, String correlationId, BigDecimal amount,
                                                PaymentMethod method, LocalDateTime occurredAt) {

        this.eventType = eventType;
        this.paymentId = paymentId;
        this.correlationId = correlationId;
        this.amount = amount;
        this.method = method;
        this.occurredAt = LocalDateTime.now();
    }

}
