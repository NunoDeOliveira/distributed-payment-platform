package com.tfg.paymentservice.model;

public enum PaymentState {
    CREATED,
    WAITING,
    PREPARING,
    COMPLETED,
    CANCELLED,
    REJECTED,
    //FAILED,
    //PENDING,
    TIMEOUT
    //FAILED
}
