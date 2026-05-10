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

                // Production service
                .route("production_route", r -> r
                        .path("/production/**").and()
                        .method(HttpMethod.GET, HttpMethod.POST)
                        .filters(f -> f
                                .stripPrefix(1).circuitBreaker(c -> {
                                    c.setName("productionCircuitBreak");
                                    c.setFallbackUri("forward:/fallback-production");
                                }).addResponseHeader("Gateway-Service", "Production-Service")
                        )
                        //.uri("http://production-service:8081")
                        .uri("http://localhost:8081")
                )

                // Delivery service
                .route("delivery_route", r -> r
                        .path("/delivery/**").and()
                        .method(HttpMethod.GET, HttpMethod.POST)
                        .filters(f -> f
                                .stripPrefix(1).circuitBreaker(c -> {
                                    c.setName("deliveryCircuitBreak");
                                    c.setFallbackUri("forward:/fallback-delivery");
                                }).addResponseHeader("Gateway-Service", "Delivery-Service")
                        )
                        //.uri("http://delivery-service:8082")
                        .uri("http://localhost:8082")
                )
                .build();
    }

}
