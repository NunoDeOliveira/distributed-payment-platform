package com.tfg.transferservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;


@Getter
@Setter
@Entity
@Table(name = "productions")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @Enumerated(EnumType.STRING)
    private TransferState state;
    
    @Column(name = "register", length = 5000)
    private String register = "";
    private int retryCount = 0;
    

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Transfer() {}

    public Transfer(int amount, TransferState state, LocalDateTime startTime) {
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
        this.register += state.name() + " " + startTime + " | ";
    }
    
    // Switch to PREPARING state and record the production start time
    public void created() {
        LocalDateTime now = LocalDateTime.now();
        this.state = TransferState.CREATED;
        this.startTime = LocalDateTime.now();
        this.register += "CREATED " + now + " | ";
    }
    
    // Switch to PREPARING state and record the production start time
    public void waiting() {
        LocalDateTime now = LocalDateTime.now();
        this.state = TransferState.WAITING;
        this.startTime = LocalDateTime.now();
        this.register += "WAITING " + now + " | ";
    }

    // Switch to PREPARING state and record the production start time
    public void start() {
        LocalDateTime now = LocalDateTime.now();
        this.state = TransferState.PREPARING;
        this.startTime = LocalDateTime.now();
        this.register += "PREPARING " + now + " | ";
    }

    // Switch to COMPLETED state and record the production start time
    public void complete() {
        this.state = TransferState.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.register += "COMPLETED " + LocalDateTime.now();
    }
    
    // Switch to CANCELLED state and record the production CANCELLED time
    public void cancelled() {
        this.state = TransferState.CANCELLED;
        this.endTime = LocalDateTime.now();
        this.register += "CANCELLED " + LocalDateTime.now();
    }
    
    
    public void reject() {
        this.state = TransferState.REJECTED;
        this.endTime = LocalDateTime.now();
        this.register += "REJECTED " + LocalDateTime.now();
    }
    
    // tIme-out if inventory fail
    public void timeout() {
        this.state = TransferState.TIMEOUT;
        this.endTime = LocalDateTime.now();
        this.register += "TIMEOUT " + LocalDateTime.now();
    }
    /*
    // When inventory connection fail 3 times the state will be failed 
    public void fail() {
        this.state = TransferState.FAILED;
        this.endTime = LocalDateTime.now();
        this.register += "FAILED " + LocalDateTime.now();
    }
    
    public void pending() {
        this.state = TransferState.PENDING;
        this.endTime = null;
        this.register += "PENDING " + LocalDateTime.now() + " | ";
    }
    
    public void incrementRetry() {
        this.retryCount++;
    }*/
    
}
