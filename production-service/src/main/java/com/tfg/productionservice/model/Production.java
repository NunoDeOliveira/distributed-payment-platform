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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public Production() {}

    public Production(int amount, ProductionState state, LocalDateTime startTime) {
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
    }

    // Switch to PREPARING state and record the production start time
    public void start() {
        this.state = ProductionState.PREPARING;
        this.startTime = LocalDateTime.now();
    }

    // Switch to COMPLETED state and record the production start time
    public void complete() {
        this.state = ProductionState.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    public void reject() {
        this.state = ProductionState.REJECTED;
        this.endTime = LocalDateTime.now();
    }
    
    // tIme-out if inventory fail
    public void timeout() {
        this.state = ProductionState.TIMEOUT;
        this.endTime = LocalDateTime.now();
    }
    
    // When inventory connection fail 3 times the state will be failed 
    public void fail() {
        this.state = ProductionState.FAILED;
        this.endTime = LocalDateTime.now();
    }
    
}
