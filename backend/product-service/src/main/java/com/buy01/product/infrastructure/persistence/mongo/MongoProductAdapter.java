package com.buy01.product.infrastructure.persistence.mongo;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;

import reactor.core.publisher.Flux;
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
        var savedProduct = springRepo.save(mapper.toDocument(product));
        return savedProduct.map(mapper::toDomain);
    }

    @Override
    public Mono<Product> findById(String id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Product> findByUserId(String userId) {
        return springRepo.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return springRepo.deleteById(id);
    }

    @Override
    public Flux<Product> findTop10ByCreatedAtBeforeOrderByCreatedAtDesc(Instant lastProduct) {
        var productList = springRepo.findTop10ByCreatedAtBeforeOrderByCreatedAtDesc(lastProduct);
        return productList.map(mapper::toDomain);
    }

    @Override
    public Flux<Product> findTop10ByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(String userId, Instant beforeTime) {
        var productList = springRepo.findTop10ByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(userId, beforeTime);
        return productList.map(mapper::toDomain);
    }
}
