package com.tfg.delivery_service.model;

import jakarta.persistence.Enumerated;

public enum DeliveryState {
    CREATED,
    RESERVED,
    ON_DELIVERY,
    COMPLETED,
    CANCELLED,
    REJECTED
    //PENDING,
    //TIMEOUT,
    //FAILED
}
