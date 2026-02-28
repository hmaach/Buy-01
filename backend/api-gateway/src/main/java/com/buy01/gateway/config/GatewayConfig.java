// package com.buy01.gateway.config;

// import org.springframework.cloud.gateway.route.RouteLocator;
// import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class GatewayConfig {

//     @Bean
//     public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//         return builder.routes()
//                 // USER SERVICE ROUTES
//                 .route("user-service", r -> r
//                         .path("/users/**")
//                         .uri("http://localhost:8081"))
//                 // PRODUCT SERVICE ROUTES
//                 .route("product-service", r -> r
//                         .path("/products/**")
//                         .uri("http://localhost:8082"))
//                 // MEDIA SERVICE ROUTES
//                 .route("media-service", r -> r
//                         .path("/media/**")
//                         .uri("http://localhost:8083"))
//                 .build();
//     }
// }
