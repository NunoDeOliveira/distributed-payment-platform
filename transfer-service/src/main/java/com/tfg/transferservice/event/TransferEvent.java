package com.tfg.transferservice.event;

public class TransferEvent {

    private String eventType;
    private Long productionId;
    private Integer amount;

    public TransferEvent() {
    }

    public TransferEvent(String event, Long productionId, Integer amount) {
        this.eventType = event;
        this.productionId = productionId;
        this.amount = amount;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getTransferId() {
        return productionId;
    }

    public Integer getAmount() {
        return amount;
    }

}
