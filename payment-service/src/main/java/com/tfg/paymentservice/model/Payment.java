package com.tfg.paymentservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


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

    public Payment(String correlationId, BigDecimal amount, PaymentMethod method, PaymentState state, LocalDateTime startAt) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.method = method;
        this.state = state;
        this.startAt = startAt;
        this.endAt = null;
        this.register = state.name() + " " + startAt + " | ";
    }
    
    // Switch to PREPARING state and record the payment start time
    public void created() {
        LocalDateTime now = LocalDateTime.now();
        this.state = PaymentState.CREATED;
        this.startAt = LocalDateTime.now();
        this.register += "CREATED " + now + " | ";
    }

    // Switch to COMPLETED state and record the payment start time
    public void complete() {
        this.state = PaymentState.COMPLETED;
        this.endAt = LocalDateTime.now();
        this.register += "COMPLETED " + LocalDateTime.now();
    }
    
    // Switch to CANCELLED state and record the payment CANCELLED time
    public void cancelled() {
        this.state = PaymentState.CANCELLED;
        this.endAt = LocalDateTime.now();
        this.register += "CANCELLED " + LocalDateTime.now();
    }
    
    
    public void reject() {
        this.state = PaymentState.REJECTED;
        this.endAt = LocalDateTime.now();
        this.register += "REJECTED " + LocalDateTime.now();
    }
    
    // tIme-out if inventory fail
    public void timeout() {
        this.state = PaymentState.FAILED;
        this.endAt = LocalDateTime.now();
        this.register += "FAILED " + LocalDateTime.now();
    }

    /*private void addRegister(String state, LocalDateTime time) {
        if (this.register == null) {
            this.register = "";
        }

        this.register += state + " " + time + " | ";
    }*/
    
}
