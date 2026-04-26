package com.tfg.delivery_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "deliveries")

public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @Enumerated(EnumType.STRING)
    private DeliveryState state;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Delivery() {
    }

    public Delivery(int amount, DeliveryState state, LocalDateTime startTime) {
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
    }

    // Switch state READY_FOR_DELIVERY state and record the delivery start time
    public void start() {
        this.state = DeliveryState.READY_FOR_DELIVERY;
        this.startTime = LocalDateTime.now();
    }

    // Switch state COMPLETED state and record the delivery start time
    public void complete() {
        this.state = DeliveryState.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    // Switch state to REJECTED and record time
    public void reject() {
        this.state = DeliveryState.REJECTED;
        this.endTime = LocalDateTime.now();
    }

}
