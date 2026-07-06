package com.tfg.paymentservice.service;

import com.tfg.paymentservice.message.PaymentPublish;
import com.tfg.paymentservice.model.Payment;
import com.tfg.paymentservice.model.PaymentMethod;
import com.tfg.paymentservice.model.PaymentState;
import com.tfg.paymentservice.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
//import org.springframework.scheduling.annotation.Scheduled;


@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentPublish paymentPublish;

    public PaymentService(PaymentRepository paymentRepository, PaymentPublish paymentPublish) {
        this.paymentRepository = paymentRepository;
        this.paymentPublish = paymentPublish;
    }


    // Given an ID and amount of Payments create a Payment
    @Transactional
    public Payment createPayment(BigDecimal amount, PaymentMethod method) {
        // Check input data
        if (amount == null || method == null) {
            throw new IllegalArgumentException("Amount and method cannot be null");
        }

        // Define a correlation Id for the saga flow
        String correlationId = UUID.randomUUID().toString();

        // Create a new payment object
        Payment newPayment = new Payment(correlationId, amount, method, PaymentState.CREATED, LocalDateTime.now());

        // Storage the object payment with the state created
        Payment storedPayment = paymentRepository.save(newPayment);

        // Publish on RabbitMQ for consume the new payment created
        paymentPublish.publishPaymentCreated(storedPayment.getId(), storedPayment.getCorrelationId(),
                                                storedPayment.getAmount(), storedPayment.getMethod());

        return storedPayment;
    }

    @Transactional
    // When the payment is completed save payment in repository
    // and publish an event on RabbitMQ
    public void completePayment(String correlationId) {
        // Check input data
        if (correlationId == null) {
            return;
        }

        // Given an Id get the payment
        Payment payment = paymentRepository.findByCorrelationId(correlationId).orElse(null);
        if (payment == null) {
            return;
        }

        // Check last state
        if (payment.getState() == PaymentState.CANCELLED ||
                payment.getState() == PaymentState.COMPLETED) {
            return;
        }

        // Save the payment as completed
        payment.complete();
        paymentRepository.save(payment);
        // Notify by api gateway
    }

    @Transactional
    // Given an id of payment cancell that payment
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            return;
        }
        // Check the current state before save
        if (payment.getState() == PaymentState.COMPLETED || payment.getState() == PaymentState.CANCELLED ||
                payment.getState() == PaymentState.REJECTED || payment.getState() == PaymentState.FAILED) {
            return;
        }

        // Save the state into repository
        payment.cancelled();
        paymentRepository.save(payment);
        // Publish on RabbitMQ for consume the new payment created
        paymentPublish.publishPaymentCancelled(payment.getId(), payment.getCorrelationId());
    }

    @Transactional
    // Method for saving a rejected payment in the DB
    public void releasePayment(String correlationId) {
        Payment payment = paymentRepository.findByCorrelationId(correlationId).orElse(null);
        if (payment == null) {
            return;
        }

        // Check the current state before save
        if (payment.getState() == PaymentState.COMPLETED || payment.getState() == PaymentState.CANCELLED ||
                payment.getState() == PaymentState.REJECTED || payment.getState() == PaymentState.FAILED) {
            return;
        }

        // Save the state into repository
        payment.reject();
        paymentRepository.save(payment);
        // Notify by api gateway
    }

    // Get payment by ID
    public Payment getPayment(Long id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        Payment paymentToReturn = payment.orElseThrow(() -> new RuntimeException("Payment " + id + "not found"));

        return paymentToReturn;
    }

    // Get all the payment from the repository
    // This query is to return to the user
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    
}
