package com.buy01.product.infrastructure.persistence.mongo;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface SpringDataProductRepository extends ReactiveMongoRepository<ProductDocument, String> {
    Flux<ProductDocument> findByUserId(String userId);
}
