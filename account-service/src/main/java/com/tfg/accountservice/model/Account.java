package com.tfg.accountservice.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, unique = true)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountState state;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(length = 500)
    private String register;


    public Account() {
    }

    public Account(String correlationId, BigDecimal balance) {
        this.correlationId = correlationId;
        this.balance = balance;
        //this.reservedAmount = BigDecimal.ZERO;
        this.state = AccountState.RESERVED;
        this.startTime = LocalDateTime.now();
        this.register = "CREATED " + this.startAt + " | ";
    }


    public void reserved(BigDecimal amount) {
        //this.reservedAmount = amount;
        //this.balance = this.balance.subtract(amount);
        this.state = AccountState.RESERVED;
        this.register += "RESERVED " + LocalDateTime.now() + " | ";
    }

    public void confirmed() {
        this.state = AccountState.CONFIRMED;
        this.endTime = LocalDateTime.now();
        this.register += "CONFIRMED " + endTime + " | ";
    }

    public void released() {
        //this.balance = this.balance.add(this.reservedAmount);
        //this.reservedAmount = BigDecimal.ZERO;
        this.state = AccountState.RELEASED;
        this.endTime = LocalDateTime.now();
        this.register += "RELEASED " + endTime + " | ";
    }

    public void canceled() {
        //this.balance = this.balance.add(this.reservedAmount);
        //this.reservedAmount = BigDecimal.ZERO;
        this.state = AccountState.CANCELED;
        this.endTime = LocalDateTime.now();
        this.register += "CANCELED " + endTime + " | ";
    }

    public void rejected() {
        this.state = AccountState.REJECTED;
        this.endTime = LocalDateTime.now();
        this.register += "REJECTED " + endTime + " | ";
    }
}