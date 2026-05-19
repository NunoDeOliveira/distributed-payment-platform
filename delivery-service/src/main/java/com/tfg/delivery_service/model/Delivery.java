package com.tfg.delivery_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;


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
    
    @Column(name = "register", length = 5000)
    private String register = "";

    public Delivery() {
    }

    public Delivery(int amount, DeliveryState state, LocalDateTime startTime) {
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
        this.register = "RESERVING " + startTime + " | ";
    }

    // Switch state READY_FOR_DELIVERY state and record the delivery start time
    public void start() {
        this.state = DeliveryState.ON_DELIVERY;
        this.startTime = LocalDateTime.now();
        this.register += "ON_DELIVERY " + LocalDateTime.now() + " | ";
    }

    // Switch state COMPLETED state and record the delivery start time
    public void complete() {
        this.state = DeliveryState.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.register += "COMPLETED " + LocalDateTime.now();
    }

    // Switch state to REJECTED and record time
    public void reject() {
        this.state = DeliveryState.REJECTED;
        this.endTime = LocalDateTime.now();
    }
    
    // Switch state to TIMEOUT and record time
    public void timeout() {
        this.state = DeliveryState.TIMEOUT;
        this.endTime = LocalDateTime.now();
        this.register += "TIMEOUT " + LocalDateTime.now();
    }
    
    // Switch state to FAILED and record time
    public void fail() {
        this.state = DeliveryState.FAILED;
        this.endTime = LocalDateTime.now();
        this.register += "FAILED " + LocalDateTime.now();
    }
    
    public void pending() {
        this.state = DeliveryState.PENDING;
        this.endTime = null;
        this.register += "PENDING " + LocalDateTime.now() + " | ";
    }
    
    // Switch  to reserving state and record time
    public void reserving() {
        this.state = DeliveryState.RESERVING;
        this.endTime = LocalDateTime.now();
        this.register += "RESERVING " + LocalDateTime.now() + " | ";
    }

}
