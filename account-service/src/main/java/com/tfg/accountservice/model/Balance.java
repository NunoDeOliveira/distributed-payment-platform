package com.tfg.accountservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Entity
@Table(name = "balances")
public class Balance {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private Long version;

    @Column(unique = true)
    private String correlationId;

    // Amount of operation
    //@Column(nullable = false)
    //private BigDecimal amount;

    // balance of account
    @Column(nullable = false)
    private BigDecimal balanceAccount;

    @Enumerated(EnumType.STRING)
    private BalanceState state;

    private LocalDateTime time;

    @Column(length = 1000)
    private String register;


    public Balance() {
        this.register = "";
    }

    public Balance(String correlationId, BigDecimal balanceAccount, BalanceState state) {
        this.correlationId = correlationId;
        this.balanceAccount = balanceAccount;
        this.state = state;
        this.time = null;
        this.register = "";
    }

    public void reserved() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = BalanceState.RESERVED;
        this.time = now;
        this.register += "RESERVED " + now.toLocalTime() + " | ";
    }

    public void confirm() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = BalanceState.CONFIRMED;
        this.time = now;
        this.register += "CONFIRMED " + now.toLocalTime() + " | ";
    }

    public void rejected() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = BalanceState.REJECTED;
        this.time = now;
        this.register += "REJECTED " + now.toLocalTime() + " | ";
    }

    public void released() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = BalanceState.RELEASED;
        this.time = now;
        this.register += "RELEASED " + now.toLocalTime() + " | ";
    }

    public void canceled() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = BalanceState.CANCELED;
        this.time = now;
        this.register += "CANCELED " + now.toLocalTime() + " | ";
    }
}
