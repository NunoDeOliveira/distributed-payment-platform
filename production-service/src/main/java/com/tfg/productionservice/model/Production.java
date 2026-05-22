package com.tfg.productionservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;


@Getter
@Setter
@Entity
@Table(name = "productions")
public class Production {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @Enumerated(EnumType.STRING)
    private ProductionState state;
    
    @Column(name = "register", length = 5000)
    private String register = "";
    private int retryCount = 0;
    
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public Production() {}

    public Production(int amount, ProductionState state, LocalDateTime startTime) {
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
        this.register += "WAITING " + startTime + " | ";
    }
    
    // Switch to PREPARING state and record the production start time
    public void waitting() {
        LocalDateTime now = LocalDateTime.now();
        this.state = ProductionState.WAITING;
        this.startTime = LocalDateTime.now();
        this.register += "WAITING " + now + " | ";
    }

    // Switch to PREPARING state and record the production start time
    public void start() {
        LocalDateTime now = LocalDateTime.now();
        this.state = ProductionState.PREPARING;
        this.startTime = LocalDateTime.now();
        this.register += "PREPARING " + now + " | ";
    }

    // Switch to COMPLETED state and record the production start time
    public void complete() {
        this.state = ProductionState.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.register += "COMPLETED " + LocalDateTime.now();
    }

    public void reject() {
        this.state = ProductionState.REJECTED;
        this.endTime = LocalDateTime.now();
        this.register += "REJECTED " + LocalDateTime.now();
    }
    
    // tIme-out if inventory fail
    public void timeout() {
        this.state = ProductionState.TIMEOUT;
        this.endTime = LocalDateTime.now();
        this.register += "TIMEOUT " + LocalDateTime.now();
    }
    
    // When inventory connection fail 3 times the state will be failed 
    public void fail() {
        this.state = ProductionState.FAILED;
        this.endTime = LocalDateTime.now();
        this.register += "FAILED " + LocalDateTime.now();
    }
    
    public void pending() {
        this.state = ProductionState.PENDING;
        this.endTime = null;
        this.register += "PENDING " + LocalDateTime.now() + " | ";
    }
    
    public void incrementRetry() {
        this.retryCount++;
    }
    
}
