package com.tfg.api_gateway.routing;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayRouting {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()

                // Payment service
                .route("payment_route", r -> r
                                .path("/payments/**")
                                .and()
                                .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE)
                                .filters(f -> f
                                        .stripPrefix(0)
                                        .circuitBreaker(c -> {
                                            c.setName("paymentCircuitBreak");
                                            c.setFallbackUri("forward:/fallback-payment");
                                        }).addResponseHeader("Gateway-Service", "Payment-Service")
                                )
                                .uri("http://payment-service:8081")
                                //.uri("http://localhost:8081")
                )

                .route("commission_route", r -> r.path("/commissions/**").and()
                        .method(HttpMethod.GET).filters(f -> f.stripPrefix(0)
                                .circuitBreaker(c -> {
                                    c.setName("commissionCircuitBreaker");
                                    c.setFallbackUri("forward:/fallback-commission");
                                }).addResponseHeader("Gateway-Service", "Commission-Service"))
                        .uri("http://commission-service:8082")
                        //.uri("http://localhost:8082")
                )

                .route("account_route", r -> r
                                .path("/balances/**")
                                .and()
                                .method(HttpMethod.GET, HttpMethod.POST)
                                .filters(f -> f
                                        .stripPrefix(0)
                                        .circuitBreaker(c -> {
                                            c.setName("accountCircuitBreaker");
                                            c.setFallbackUri("forward:/fallback-account");
                                        }).addResponseHeader("Gateway-Service", "Account-Service"))
                                .uri("http://account-service:8083")
                                //.uri("http://localhost:8083")
                )

                .route("ledger_route", r -> r.path("/movements/**").and()
                        .method(HttpMethod.GET).filters(f -> f.stripPrefix(0)
                                .circuitBreaker(c -> {
                                    c.setName("ledgerCircuitBreaker");
                                    c.setFallbackUri("forward:/fallback-ledger");
                                }).addResponseHeader("Gateway-Service", "Ledger-Service"))
                        .uri("http://ledger-service:8084")
                        //.uri("http://localhost:8084")
                )

                .build();
    }

}

