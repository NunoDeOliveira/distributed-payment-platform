package com.tfg.commissionservice.model;

import java.math.BigDecimal;

public enum CommissionMethod {

    INTERNATIONAL_TRANSFER("0.02"),
    BILL_PAYMENT("0.005"),
    BIZUM("0");

    private final BigDecimal commissionRate;

    CommissionMethod(String value) {
        this.commissionRate = new BigDecimal(value);
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

}