package com.tfg.accountservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "account_balances")
public class AccountBalance {

    @Id
    private Long id;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance;

    protected AccountBalance() {
    }

    public AccountBalance(Long id, BigDecimal currentBalance) {
        this.id = id;
        this.currentBalance = currentBalance;
    }

}
