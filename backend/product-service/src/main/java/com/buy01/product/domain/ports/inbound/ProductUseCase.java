package com.buy01.product.domain.ports.inbound;

import com.buy01.product.domain.model.Product;

import reactor.core.publisher.Mono;

public interface ProductUseCase {
    Mono<Product> createProduct(Product product);
}
