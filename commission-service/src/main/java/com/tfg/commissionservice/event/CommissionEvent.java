package com.tfg.commissionservice.event;

import com.tfg.commissionservice.model.CommissionMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class CommissionEvent {

    private String eventType;
    private String correlationId;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private String method;

    public CommissionEvent() {
    }

    public CommissionEvent(String eventType, String correlationId,
                           BigDecimal amount, BigDecimal totalAmount, String method) {

        this.eventType = eventType;
        this.correlationId = correlationId;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.method = null;
    }

}
