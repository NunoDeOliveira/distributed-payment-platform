package com.tfg.commissionservice.event;

import com.tfg.commissionservice.model.CommissionMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class CommissionEvent {

    private String eventType;
    private Long paymentId;
    private String correlationId;
    private BigDecimal totalAmount;
    private String method;

    public CommissionEvent() {
    }

    public CommissionEvent(String eventType, Long paymentId,String correlationId, BigDecimal totalAmount, String method) {

        this.eventType = eventType;
        this.paymentId = paymentId;
        this.correlationId = correlationId;
        this.totalAmount = totalAmount;
        this.method = null;
    }

}
