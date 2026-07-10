package com.tfg.paymentservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentState state;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Column(length = 1000)
    private String register;


    protected Payment() {}

    public Payment(String correlationId, BigDecimal amount, PaymentMethod method,
                   PaymentState state, LocalDateTime startAt) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.method = method;
        this.state = state;
        this.startAt = startAt.truncatedTo(ChronoUnit.SECONDS);
        this.endAt = null;
        this.register = state.name() + " " + startAt.toLocalTime().truncatedTo(ChronoUnit.SECONDS) + " | ";
    }

    // Switch to CREATED state and record the payment start time
    public void created() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = PaymentState.CREATED;
        this.startAt = now;
        this.register += "CREATED " + now.toLocalTime() + " | ";
    }

    // Switch to COMPLETED state and record the payment end time
    public void complete() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = PaymentState.COMPLETED;
        this.endAt = now;
        this.register += "COMPLETED " + now.toLocalTime() + " | ";
    }

    // Switch to CANCELLED state and record the payment end time
    public void canceled() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = PaymentState.CANCELLED;
        this.endAt = now;
        this.register += "CANCELLED " + now.toLocalTime() + " | ";
    }

    public void reject() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = PaymentState.REJECTED;
        this.endAt = now;
        this.register += "REJECTED " + now.toLocalTime() + " | ";
    }

    // Timeout if payment fails
    public void timeout() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.state = PaymentState.FAILED;
        this.endAt = now;
        this.register += "FAILED " + now.toLocalTime() + " | ";
    }
}