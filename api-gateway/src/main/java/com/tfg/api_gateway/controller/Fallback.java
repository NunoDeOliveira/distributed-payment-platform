package com.tfg.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class Fallback {

    @GetMapping("/fallback-payment")
    public Mono<ResponseEntity<String>> fallbackPayment() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Payment Service unavailable"));
    }

    @GetMapping("/fallback-commission")
    public Mono<ResponseEntity<String>> fallbackCommission() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Commission Service unavailable"));
    }

    @GetMapping("/fallback-account")
    public Mono<ResponseEntity<String>> fallbackAccount() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Account Service unavailable"));
    }

    @GetMapping("/fallback-ledger")
    public Mono<ResponseEntity<String>> fallbackLedger() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Ledger Service unavailable"));
    }
}
