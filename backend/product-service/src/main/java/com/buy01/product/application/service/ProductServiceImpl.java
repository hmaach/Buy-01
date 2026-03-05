package com.buy01.product.application.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;
import com.buy01.product.infrastructure.messaging.ImagesLinkedEvent;
import com.buy01.product.infrastructure.persistence.mongo.ProductDocument;
import com.buy01.product.infrastructure.web.dto.ProductList;
import com.buy01.product.infrastructure.web.dto.ProductResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final KafkaTemplate<String, ImagesLinkedEvent> kafkaTemplate;
    private final ImageService imageService;

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
                .flatMap(p -> {
                    var saved = productRepository.save(p);
                    return saved.map(pp -> pp.toBuilder().imagesIds(imagesIds).build());
                })
                .doOnNext(savedProduct -> sendKafkaEvent(savedProduct, imagesIds))
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

    @Override
    public Mono<ProductResponse> getProductWithImages(String productId) {
        Mono<Product> productMono = productRepository.findById(productId);

        Mono<List<String>> imagesMono = imageService.getProductImages(productId);

        return Mono.zip(productMono, imagesMono)
                .map(tuple -> new ProductResponse(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Flux<ProductList> getProductsList(Instant beforeTime) {
        return productRepository.findTop10ByCreatedAtBeforeOrderByCreatedAtDesc(beforeTime)
                .collectList()
                .flatMapMany(products -> {
                    if (products.isEmpty()) {
                        return Flux.empty();
                    }

                    List<String> ids = products.stream().map(Product::getId).toList();
                    System.out.println("ids: " + ids);

                    var images = imageService.getImagesBatch(ids);
                    images.subscribe(v -> System.out.println("/////////>  " + v));

                    return images.map(urlMap -> products.stream()
                            .map(p -> new ProductList(
                                    p.getId(),
                                    p.getName(),
                                    p.getPrice(),
                                    p.getCreatedAt(),
                                    urlMap.getOrDefault(p.getId(), "https://via.placeholder.com/300")))
                            .collect(Collectors.toList()))
                            .flatMapMany(Flux::fromIterable);
                });
    }

    private void sendKafkaEvent(Product savedProduct, List<String> imagesIds) {
        if (!imagesIds.isEmpty()) {
            ImagesLinkedEvent event = new ImagesLinkedEvent(
                    savedProduct.getId(),
                    imagesIds);

            kafkaTemplate.send(
                    TOPIC_IMAGES_LINKED,
                    savedProduct.getId(),
                    event);

            log.info("Kafka event sent: product={}, media count={}",
                    savedProduct.getId(), imagesIds.size());
        } else {
            log.debug("No images to link for product {}", savedProduct.getId());
        }
    }
}
