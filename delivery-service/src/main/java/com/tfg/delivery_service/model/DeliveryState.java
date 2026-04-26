package com.tfg.delivery_service.model;

import jakarta.persistence.Enumerated;

public enum DeliveryState {
    PENDING,
    READY_FOR_DELIVERY,
    COMPLETED,
    REJECTED
}
