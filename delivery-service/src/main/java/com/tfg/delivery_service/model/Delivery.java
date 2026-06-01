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
    private Long productionId;

    private int amount;
    private int retryCount = 0;

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
        this.register = state.name() + " " + startTime + " | ";
    }
    
    // Switch to CREATED state and record the production start time
    public void created() {
        LocalDateTime now = LocalDateTime.now();
        this.state = DeliveryState.CREATED;
        this.startTime = LocalDateTime.now();
        this.register += "CREATED " + now + " | ";
    }
    
    // Switch to RESERVED state and record the production start time
    public void reserved() {
        LocalDateTime now = LocalDateTime.now();
        this.state = DeliveryState.RESERVED;
        this.register += "RESERVED " + now + " | ";
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
    
    // Switch state CANCELLED state and record the delivery cancelled time
    public void cancelled() {
        this.state = DeliveryState.CANCELLED;
        this.endTime = LocalDateTime.now();
        this.register += "CANCELLED " + LocalDateTime.now();
    }
    
    
    /*
    // Switch state to REJECTED and record time
    public void reject() {
        this.state = DeliveryState.REJECTED;
        this.endTime = LocalDateTime.now();
    }*/
    
    // Switch state to TIMEOUT and record time
    public void timeout() {
        this.state = DeliveryState.TIMEOUT;
        this.endTime = LocalDateTime.now();
        this.register += "TIMEOUT " + LocalDateTime.now();
    }
    
    /*
    public void pending() {
        this.state = DeliveryState.PENDING;
        this.endTime = null;
        this.register += "PENDING " + LocalDateTime.now() + " | ";
    }
    
    // Switch  to reserving state and record time
    public void reserving() {
        this.state = DeliveryState.RESERVED;
        this.endTime = LocalDateTime.now();
        this.register += "RESERVED " + LocalDateTime.now() + " | ";
    }
    
    public void incrementRetry() {
        this.retryCount++;
    }*/

}
