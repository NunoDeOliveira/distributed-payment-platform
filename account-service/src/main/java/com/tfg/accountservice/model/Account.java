package com.tfg.accountservice.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private Long accountId;

    @Column(length = 1000)
    private String register;


    public Account() {}

    public Account(String correlationId, BigDecimal balance, String register) {
        this.correlationId = correlationId;
        this.balance = balance;
    }

    public void held(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void release(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

}
