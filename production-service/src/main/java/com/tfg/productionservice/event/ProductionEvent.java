package com.tfg.productionservice.event;

public class ProductionEvent {

    private String eventType;
    private Long productionId;
    private Integer amount;

    public ProductionEvent() {
    }

    public ProductionEvent(String event, Long productionId, Integer amount) {
        this.eventType = event;
        this.productionId = productionId;
        this.amount = amount;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getProductionId() {
        return productionId;
    }

    public Integer getAmount() {
        return amount;
    }

}
