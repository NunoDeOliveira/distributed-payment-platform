package com.tfg.ledgerservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Getter
@Setter
@Entity
@Table(name = "movements")
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
    
    @Column(name = "register", length = 1000)
    private String register = "";

    public Movement() {
    }

    public Movement(String correlationId, BigDecimal amount) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.register = "";
    }

    public Movement(String correlationId, BigDecimal amount,
                    MovementState state, LocalDateTime startTime) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
        this.register = state.name() + " " + startTime + " | ";
    }

    public void recorded() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = MovementState.RECORDED;
        this.startTime = now;
        this.register += "RECORDED " + now.toLocalTime() + " | ";
    }

    public void cancelled() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = MovementState.CANCELED;
        this.startTime = now;
        this.register += "CANCELED " + now.toLocalTime() + " | ";
    }

    // Switch state to REJECTED and record time
    public void reject() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = MovementState.REJECTED;
        this.endTime = now;
        this.register += "REJECTED " + now.toLocalTime() + " | ";
    }

    // Switch state to WAITING and record time
    public void waiting() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = MovementState.WAITING;
        this.startTime = now;
        this.register += "WAITING " + now.toLocalTime() + " | ";
    }

    // Switch state to RELEASED and record time
    public void released() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = MovementState.RELEASED;
        this.endTime = now;
        this.register += "RELEASED " + now.toLocalTime() + " | ";
    }
}
