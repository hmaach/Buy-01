package com.buy01.media.infrastructure.messaging;

import com.buy01.media.domain.model.FileStatus;
import com.buy01.media.domain.ports.outbound.MediaRepositoryPort;
import com.buy01.media.infrastructure.web.dto.ImagesLinkedEvent;
import com.buy01.media.infrastructure.web.dto.ProductDeletedEvent;
import com.buy01.media.domain.model.Media;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaStatusListener {

    private final MediaRepositoryPort mediaRepository;

    @KafkaListener(topics = "${kafka.topics.images-linked:images-linked}", groupId = "media-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onImagesLinked(ImagesLinkedEvent event) {
        log.info("Kafka: Processing link event for product: {}, media count: {}",
                event.productId(), event.mediaIds().size());

        for (String mediaId : event.mediaIds()) {
            try {
                mediaRepository.findById(mediaId)
                        .ifPresent(media -> {
                            if (media.getStatus() == FileStatus.PENDING) {
                                media.setStatus(FileStatus.LINKED);
                                media.setProductId(event.productId());
                                mediaRepository.save(media);
                                log.debug("Kafka: Updated media {} to LINKED", mediaId);
                            }
                        });
            } catch (Exception e) {
                log.error("Kafka: Failed to update media id: {}", mediaId, e);
                // TODO optional: send to DLQ or retry
            }
        }
    }

    @KafkaListener(topics = "${kafka.topics.product-deleted:product-deleted}", groupId = "media-service-group", containerFactory = "productDeletedKafkaListenerContainerFactory")
    public void onProductDeleted(ProductDeletedEvent event) {
        log.info("Kafka: Processing product-deleted event for product: {}", event.productId());

        try {
            List<Media> medias = mediaRepository.findByProductId(event.productId());

            for (Media media : medias) {
                try {
                    mediaRepository.deleteById(media.getImagePath());
                    log.debug("Kafka: Deleted media {} for product {}", media.getImagePath(), event.productId());
                } catch (Exception e) {
                    log.error("Kafka: Failed to delete media id: {}", media.getImagePath(), e);
                }
            }
        } catch (Exception e) {
            log.error("Kafka: Failed processing product-deleted event for product: {}", event.productId(), e);
        }
    }

}