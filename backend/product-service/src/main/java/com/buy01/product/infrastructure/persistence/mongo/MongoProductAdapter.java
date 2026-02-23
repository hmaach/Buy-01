package com.buy01.product.infrastructure.persistence.mongo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;
import com.buy01.product.infrastructure.web.dto.ProductCreatedEvent;

import reactor.core.publisher.Mono;

@Component
public class MongoProductAdapter implements ProductRepositoryPort {
    private final SpringDataProductRepository springRepo;
    private final ProductDocumentMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${kafka.topic:product-created-topic}")
    private String topic;

    public MongoProductAdapter(SpringDataProductRepository springRepo, ProductDocumentMapper mapper,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.springRepo = springRepo;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Product> save(Product product) {
        var savedProduct = springRepo.save(mapper.toDocument(product));

        ProductCreatedEvent event = new ProductCreatedEvent(
                product.getId(),
                product.getMediaIds(),
                product.getUserId());

        kafkaTemplate.send(topic, event);

        return savedProduct.map(mapper::toDomain);
    }

    @Override
    public Mono<Product> findById(String id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(String id) {
        springRepo.deleteById(id);
    }
}