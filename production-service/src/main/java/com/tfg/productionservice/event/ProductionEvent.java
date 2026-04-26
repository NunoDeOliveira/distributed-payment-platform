package com.tfg.productionservice.event;

public class ProductionEvent {
    private String event;
    private Long productionId;
    private Integer amount;

    public ProductionEvent() {
    }

    public ProductionEvent(String event, Long productionId, Integer amount) {
        this.event = event;
        this.productionId = productionId;
        this.amount = amount;
    }

    public String getEvent() {
        return event;
    }

    public Long getProductionId() {
        return productionId;
    }

    public Integer getAmount() {
        return amount;
    }

}
