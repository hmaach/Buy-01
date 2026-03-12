package com.buy01.product.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.buy01.product.application.events.UserDeletedEvent;
import com.buy01.product.application.service.ProductServiceImpl;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserListener {

    private final ProductRepositoryPort productRepository;
    private final ProductServiceImpl productService;

    @KafkaListener(topics = "${kafka.topics.user-deleted:user-deleted}", groupId = "product-service-group", containerFactory = "userDeletedKafkaListenerContainerFactory")
    public void onUserDeleted(UserDeletedEvent event) {
        log.info("Kafka: Processing user-deleted event for user: {}", event.userId());

        String userId = event.userId();

        productRepository.findByUserId(userId)
                .flatMap(p -> productService.deleteProduct(p.getId(), userId))
                .doOnComplete(() -> log.info("All products deleted for user: {}", userId))
                .doOnError(e -> log.error("Failed to delete products for user: {}", userId, e))
                .subscribe();

    }

}
