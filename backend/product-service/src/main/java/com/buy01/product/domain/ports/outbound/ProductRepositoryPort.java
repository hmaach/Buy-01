package com.buy01.product.domain.ports.outbound;

import java.time.Instant;

import com.buy01.product.domain.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepositoryPort {
    Mono<Product> save(Product product);

    Mono<Product> findById(String id);

    void deleteById(String id);

    Flux<Product> findTop10ByCreatedAtBeforeOrderByCreatedAtDesc(Instant lastProduct);
}
