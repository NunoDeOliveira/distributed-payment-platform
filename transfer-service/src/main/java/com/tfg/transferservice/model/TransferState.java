package com.tfg.transferservice.model;

public enum TransferState {
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
