package com.buy01.product.domain.ports.outbound;

import com.buy01.product.domain.model.Product;

import reactor.core.publisher.Mono;

public interface ProductRepositoryPort {
    Mono<Product> save(Product product);
}
