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

    public record CreatePaymentRequest(BigDecimal amount, PaymentMethod method) {}

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    // This method receive JSON and convert the JSON en CreatePaymentRequest
    @PostMapping
    public Payment createPayment(@RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request.amount(), request.method());
    }

    @DeleteMapping("/{id}")
    public void cancelPaymentByUser(@PathVariable Long id) {
        paymentService.cancelPayment(id);
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

}
