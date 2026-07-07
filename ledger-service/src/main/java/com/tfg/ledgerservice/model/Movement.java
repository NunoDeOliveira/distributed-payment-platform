package com.tfg.ledgerservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "deliveries")
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementState state;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Column(name = "register", length = 5000)
    private String register = "";

    public Movement() {
    }
    

    public Movement(String correlationId, BigDecimal amount,
                    MovementState state, LocalDateTime startTime) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
        this.register = state.name() + " " + startTime + " | ";
    }
    
    // Switch to RECORDED state and record the production start time
    public void recorded() {
        LocalDateTime now = LocalDateTime.now();
        this.state = MovementState.RECORDED;
        this.startTime = LocalDateTime.now();
        this.register += "RECORDED " + now + " | ";
    }

    // Switch state canceled state and record the delivery start time
    public void cancelled() {
        this.state = MovementState.CANCELED;
        this.startTime = LocalDateTime.now();
        this.register += "CANCELED " + LocalDateTime.now() + " | ";
    }

    // Switch state to REJECTED and record time
    public void reject() {
        this.state = MovementState.REJECTED;
        this.endTime = LocalDateTime.now();
        this.register += "REJECTED " + LocalDateTime.now();
    }

    // Switch state to REJECTED and record time
    public void failed() {
        this.state = MovementState.FAILED;
        this.endTime = LocalDateTime.now();
        this.register += "FAILED " + LocalDateTime.now();
    }
}
