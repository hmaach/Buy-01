package com.buy01.product.domain.service;

import java.time.Instant;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;

import reactor.core.publisher.Mono;

public class ProductServiceImpl implements ProductUseCase {
    private final ProductRepositoryPort productRepository;

    public ProductServiceImpl(ProductRepositoryPort pr) {
        this.productRepository = pr;
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        product.validate();
        Instant now = Instant.now();
        return Mono.just(product)
                .map(p -> p.toBuilder()
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .flatMap(productRepository::save);
    }

    public Mono<Product> updateProduct(Product update, String id, String userId) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found")))
                .flatMap(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        return Mono.error(new SecurityException("Not your product"));
                    }

                    Product updatedProduct = existing.toBuilder()
                            .name(update.getName())
                            .description(update.getDescription())
                            .mediaIds(update.getMediaIds())
                            .price(update.getPrice())
                            .quantity(update.getQuantity())
                            .updatedAt(Instant.now())
                            .build();

                    return productRepository.save(updatedProduct);
                });
    }

}
