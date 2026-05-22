package com.tfg.delivery_service.model;

import jakarta.persistence.Enumerated;

public enum DeliveryState {
    PENDING,
    RESERVED,
    ON_DELIVERY,
    COMPLETED,
    REJECTED,
    TIMEOUT,
    FAILED
}
