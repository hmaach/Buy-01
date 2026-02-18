package com.buy01.product.infrastructure.persistence.mongo;

import org.springframework.stereotype.Component;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;

import reactor.core.publisher.Mono;

@Component
public class MongoProductAdapter implements ProductRepositoryPort {
    private final SpringDataProductRepository springRepo;
    private final ProductDocumentMapper mapper;

    public MongoProductAdapter(SpringDataProductRepository springRepo, ProductDocumentMapper mapper) {
        this.springRepo = springRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> save(Product product) {
        return Mono.just(mapper.toDocument(product))
                .flatMap(springRepo::save)
                .map(mapper::toDomain);
    }
}