package com.tfg.accountservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "account_operations")
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountState state;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(length = 1000)
    private String register;


    public Operation() {}

    public Operation(Long accountId, String correlationId, BigDecimal amount,
                     AccountState state, LocalDateTime startTime, LocalDateTime endTime) {
        this.accountId = accountId;
        this.correlationId = correlationId;
        this.amount = amount;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
        this.register = register;
    }

    // Switch to DEBITED state and record the payment start time
    public void held() {
        LocalDateTime now = LocalDateTime.now();
        this.state = AccountState.HELD;
        this.startTime = LocalDateTime.now();
        this.register += "HELD " + now + " | ";
    }

    // Switch to DEBITED state and record the payment start time
    public void deducted() {
        LocalDateTime now = LocalDateTime.now();
        this.state = AccountState.DEBITED;
        this.startTime = LocalDateTime.now();
        this.register += "DEBITED " + LocalDateTime.now();
    }

    // Switch to REJECTED state and record the payment start time
    public void rejected() {
        this.state = AccountState.REJECTED;
        this.endTime = LocalDateTime.now();
        this.register += "REJECTED " + LocalDateTime.now();
    }

    // Switch to RELEASED state and record the payment RELEASED time
    public void released() {
        this.state = AccountState.RELEASED;
        this.endTime = LocalDateTime.now();
        this.register += "RELEASED " + LocalDateTime.now();
    }


    public void failed() {
        this.state = AccountState.FAILED;
        this.endTime = LocalDateTime.now();
        this.register += "FAILED " + LocalDateTime.now();
    }

}
