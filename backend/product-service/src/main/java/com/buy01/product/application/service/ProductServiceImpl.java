package com.buy01.product.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;
import com.buy01.product.infrastructure.messaging.ImagesLinkedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductUseCase {
    private final ProductRepositoryPort productRepository;
    private final KafkaTemplate<String, ImagesLinkedEvent> kafkaTemplate;

    private static final String TOPIC_IMAGES_LINKED = "images-linked";

    @Override
    public Mono<Product> createProduct(Product product, List<String> imagesIds) {
        product.validate();

        Instant now = Instant.now();
        
        return Mono.just(product)
                .map(p -> p.toBuilder()
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .flatMap(productRepository::save)
                .doOnNext(savedProduct -> {
                    // Send Kafka event only after successful save
                    if (!imagesIds.isEmpty()) {
                        ImagesLinkedEvent event = new ImagesLinkedEvent(
                                savedProduct.getId(),
                                imagesIds);

                        kafkaTemplate.send(
                                TOPIC_IMAGES_LINKED,
                                savedProduct.getId(), // key = product ID (good for partitioning)
                                event);

                        log.info("Kafka event sent: product={}, media count={}",
                                savedProduct.getId(), imagesIds.size());
                    } else {
                        log.debug("No images to link for product {}", savedProduct.getId());
                    }
                })
                .doOnError(e -> log.error("Failed to create product or send event", e));
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
                            .price(update.getPrice())
                            .quantity(update.getQuantity())
                            .updatedAt(Instant.now())
                            .build();

                    return productRepository.save(updatedProduct);
                });
    }

}
