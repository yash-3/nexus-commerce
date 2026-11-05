package com.nexus.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic RouteLocator Configuration
 * 
 * Defines routing rules directly as Spring Beans to guarantee route registration
 * in memory without YAML duplicate route collisions.
 */
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("product-service-route", r -> r
                        .path("/api/v1/products", "/api/v1/products/**")
                        .uri("lb://PRODUCT-SERVICE"))
                .route("order-service-route", r -> r
                        .path("/api/v1/orders", "/api/v1/orders/**")
                        .uri("lb://ORDER-SERVICE"))
                .route("payment-service-route", r -> r
                        .path("/api/v1/payments", "/api/v1/payments/**")
                        .uri("lb://PAYMENT-SERVICE"))
                .route("inventory-service-route", r -> r
                        .path("/api/v1/inventory", "/api/v1/inventory/**")
                        .uri("lb://INVENTORY-SERVICE"))
                .build();
    }
}
