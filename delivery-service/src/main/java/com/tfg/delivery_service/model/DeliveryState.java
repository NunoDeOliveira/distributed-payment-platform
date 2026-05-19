package com.tfg.delivery_service.model;

import jakarta.persistence.Enumerated;

public enum DeliveryState {
    PENDING,
    RESERVING,
    ON_DELIVERY,
    COMPLETED,
    REJECTED,
    TIMEOUT,
    FAILED
}
