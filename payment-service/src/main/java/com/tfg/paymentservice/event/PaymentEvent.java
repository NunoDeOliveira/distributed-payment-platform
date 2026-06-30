package com.tfg.paymentservice.event;

public class PaymentEvent {

    private String eventType;
    private Long paymentId;
    private Integer amount;

    public PaymentEvent() {
    }

    public PaymentEvent(String event, Long paymentId, Integer amount) {
        this.eventType = event;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Integer getAmount() {
        return amount;
    }

}
