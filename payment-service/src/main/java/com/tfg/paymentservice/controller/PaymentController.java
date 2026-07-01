package com.tfg.paymentservice.controller;

import com.tfg.paymentservice.model.Payment;
import com.tfg.paymentservice.model.PaymentMethod;
import com.tfg.paymentservice.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public record CreatePaymentRequest(BigDecimal amount, PaymentMethod method) {}

    @PostMapping
    public Payment createPayment(@RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request.amount(), request.method());
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }
    
    @DeleteMapping("/{id}")
    public void cancelPaymentByUser(@PathVariable Long id) {
        paymentService.cancelPayment(id);
    }
    
}
