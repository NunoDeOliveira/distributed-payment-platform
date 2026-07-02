package com.tfg.commissionservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "commissions")
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long operationId;

    @Column(nullable = false)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal commissionAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionState state;

    private LocalDateTime time;

    @Column(length = 1000)
    private String register;

    protected Commission() {}

    public Commission(Long operationId, String correlationId, BigDecimal amount, BigDecimal commissionAmount,
                      BigDecimal totalAmount, CommissionMethod method, CommissionState state, LocalDateTime time) {

        this.operationId = operationId;
        this.correlationId = correlationId;
        this.amount = amount;
        this.commissionAmount = commissionAmount;
        this.totalAmount = totalAmount;
        this.method = method;
        this.state = state;
        this.time = time;
        this.register = state.name() + " " + time + " | ";
    }

    public void calculated() {
        LocalDateTime now = LocalDateTime.now();
        this.state = CommissionState.CALCULATED;
        this.time = now;
        this.register += "CALCULATED " + now + " | ";
    }

    public void released() {
        LocalDateTime now = LocalDateTime.now();
        this.state = CommissionState.RELEASED;
        this.time = now;
        this.register += "RELEASED " + now + " | ";
    }

    public void rejected() {
        LocalDateTime now = LocalDateTime.now();
        this.state = CommissionState.REJECTED;
        this.time = now;
        this.register += "REJECTED " + now + " | ";
    }

    public void failed() {
        LocalDateTime now = LocalDateTime.now();
        this.state = CommissionState.FAILED;
        this.time = now;
        this.register += "FAILED " + now + " | ";
    }
}