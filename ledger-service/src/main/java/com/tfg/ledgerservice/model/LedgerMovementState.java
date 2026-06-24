package com.tfg.ledgerservice.model;

import jakarta.persistence.Enumerated;

public enum LedgerMovementState {
    CREATED,
    RESERVED,
    ON_DELIVERY,
    COMPLETED,
    CANCELLED,
    REJECTED,
    //PENDING,
    TIMEOUT
    //FAILED
}
