package com.buy01.product.domain.ports.outbound;

import java.time.Instant;

import com.buy01.product.domain.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepositoryPort {

    Mono<Product> save(Product product);

    Mono<Product> findById(String id);

    Flux<Product> findByUserId(String userId);

    Mono<Void> deleteById(String id);

    Flux<Product> findTop10ByCreatedAtBeforeOrderByCreatedAtDesc(Instant lastProduct);

    Flux<Product> findTop10ByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(String userId, Instant beforeTime);
}
