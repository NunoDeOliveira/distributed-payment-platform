package com.tfg.paymentservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;


@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentState state;
    
    @Column(name = "register", length = 5000)
    private String register = "";
    private int retryCount = 0;
    

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Payment() {}

    public Payment(int amount, PaymentState state, LocalDateTime startTime) {
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
        this.register += state.name() + " " + startTime + " | ";
    }
    
    // Switch to PREPARING state and record the payment start time
    public void created() {
        LocalDateTime now = LocalDateTime.now();
        this.state = PaymentState.CREATED;
        this.startTime = LocalDateTime.now();
        this.register += "CREATED " + now + " | ";
    }
    
    // Switch to PREPARING state and record the payment start time
    public void waiting() {
        LocalDateTime now = LocalDateTime.now();
        this.state = PaymentState.WAITING;
        this.startTime = LocalDateTime.now();
        this.register += "WAITING " + now + " | ";
    }

    // Switch to PREPARING state and record the payment start time
    public void start() {
        LocalDateTime now = LocalDateTime.now();
        this.state = PaymentState.PREPARING;
        this.startTime = LocalDateTime.now();
        this.register += "PREPARING " + now + " | ";
    }

    // Switch to COMPLETED state and record the payment start time
    public void complete() {
        this.state = PaymentState.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.register += "COMPLETED " + LocalDateTime.now();
    }
    
    // Switch to CANCELLED state and record the payment CANCELLED time
    public void cancelled() {
        this.state = PaymentState.CANCELLED;
        this.endTime = LocalDateTime.now();
        this.register += "CANCELLED " + LocalDateTime.now();
    }
    
    
    public void reject() {
        this.state = PaymentState.REJECTED;
        this.endTime = LocalDateTime.now();
        this.register += "REJECTED " + LocalDateTime.now();
    }
    
    // tIme-out if inventory fail
    public void timeout() {
        this.state = PaymentState.TIMEOUT;
        this.endTime = LocalDateTime.now();
        this.register += "TIMEOUT " + LocalDateTime.now();
    }
    /*
    // When inventory connection fail 3 times the state will be failed 
    public void fail() {
        this.state = PaymentState.FAILED;
        this.endTime = LocalDateTime.now();
        this.register += "FAILED " + LocalDateTime.now();
    }
    
    public void pending() {
        this.state = PaymentState.PENDING;
        this.endTime = null;
        this.register += "PENDING " + LocalDateTime.now() + " | ";
    }
    
    public void incrementRetry() {
        this.retryCount++;
    }*/
    
}
