package com.tfg.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class Fallback {

    @GetMapping("/fallback-production")
    public Mono<ResponseEntity<String>> fallbackProduction() {
        return Mono.just(ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Production Service unavailable")
        );
    }

    @GetMapping("/fallback-delivery")
    public Mono<ResponseEntity<String>> fallbackDelivery() {
        return Mono.just(ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Delivery Service unavailable")
        );
    }
}
