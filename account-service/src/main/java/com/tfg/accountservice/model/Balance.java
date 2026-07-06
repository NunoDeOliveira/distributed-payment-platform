package com.tfg.accountservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "balances")
public class Balance {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private BalanceState state;

    private LocalDateTime time;

    @Column(length = 1000)
    private String register;


    public Balance() {
    }

    public Balance(String correlationId, BigDecimal amount, BalanceState state) {
        this.correlationId = correlationId;
        this.amount = amount;
        //this.availableBalance = availableBalance;
        this.state = state;
        this.time = null;
        this.register = register;
    }


    // Switch to DEBITED state and record the payment start time
    public void reserved() {
        LocalDateTime now = LocalDateTime.now();
        this.state = BalanceState.RESERVED;
        this.time = LocalDateTime.now();
        this.register += "RESERVED " + now + " | ";
    }

    // Switch to DEBITED state and record the payment start time
    public void debited() {
        LocalDateTime now = LocalDateTime.now();
        this.state = BalanceState.CONFIRMED;
        this.time = LocalDateTime.now();
        this.register += "CONFIRMED " + LocalDateTime.now();
    }

    // Switch to REJECTED state and record the payment start time
    public void rejected() {
        this.state = BalanceState.REJECTED;
        this.time = LocalDateTime.now();
        this.register += "REJECTED " + LocalDateTime.now();
    }

    // Switch to RELEASED state and record the payment RELEASED time
    public void released() {
        this.state = BalanceState.RELEASED;
        this.time = LocalDateTime.now();
        this.register += "RELEASED " + LocalDateTime.now();
    }


    public void canceled() {
        this.state = BalanceState.CANCELED;
        this.time = LocalDateTime.now();
        this.register += "CANCELED " + LocalDateTime.now();
    }
}
