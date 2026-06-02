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
                .route("production_route", r -> r.path("/production/**").and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE)
                        .filters(f -> f.stripPrefix(1)
                                .addResponseHeader("Gateway-Service", "Production-Service")
                        )
                        .uri("http://production-service:8081")
                )
                .route("delivery_route", r -> r.path("/delivery/**").and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE)
                        .filters(f -> f.stripPrefix(1)
                                .addResponseHeader("Gateway-Service", "Delivery-Service")
                        )
                        .uri("http://delivery-service:8082")
                )
                .build();
    }
}
