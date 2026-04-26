package com.tfg.productionservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


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

    private LocalDateTime startTime;
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

}