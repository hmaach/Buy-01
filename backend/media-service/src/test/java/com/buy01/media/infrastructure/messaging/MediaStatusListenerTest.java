package com.buy01.media.infrastructure.messaging;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.buy01.media.domain.model.FileStatus;
import com.buy01.media.domain.model.Media;
import com.buy01.media.domain.ports.outbound.MediaRepositoryPort;
import com.buy01.media.infrastructure.web.dto.ImagesLinkedEvent;
import com.buy01.media.infrastructure.web.dto.ProductDeletedEvent;

@ExtendWith(MockitoExtension.class)
class MediaStatusListenerTest {

    @Mock
    private MediaRepositoryPort mediaRepository;

    @InjectMocks
    private MediaStatusListener mediaStatusListener;

    @Test
    void onImagesLinkedUpdatesPendingMedia() {
        Media pendingMedia = media("img-1", "/uploads/img-1.png", null, FileStatus.PENDING);
        when(mediaRepository.findById("img-1")).thenReturn(Optional.of(pendingMedia));

        mediaStatusListener.onImagesLinked(new ImagesLinkedEvent("prod-1", List.of("img-1")));

        verify(mediaRepository).findById("img-1");
        verify(mediaRepository).save(pendingMedia);
        org.assertj.core.api.Assertions.assertThat(pendingMedia.getStatus()).isEqualTo(FileStatus.LINKED);
        org.assertj.core.api.Assertions.assertThat(pendingMedia.getProductId()).isEqualTo("prod-1");
    }

    @Test
    void onImagesLinkedSkipsAlreadyLinkedMedia() {
        Media linkedMedia = media("img-2", "/uploads/img-2.png", "prod-old", FileStatus.LINKED);
        when(mediaRepository.findById("img-2")).thenReturn(Optional.of(linkedMedia));

        mediaStatusListener.onImagesLinked(new ImagesLinkedEvent("prod-9", List.of("img-2")));

        verify(mediaRepository).findById("img-2");
        verify(mediaRepository, never()).save(linkedMedia);
    }

    @Test
    void onImagesLinkedIgnoresMissingMedia() {
        when(mediaRepository.findById("missing")).thenReturn(Optional.empty());

        mediaStatusListener.onImagesLinked(new ImagesLinkedEvent("prod-1", List.of("missing")));

        verify(mediaRepository).findById("missing");
        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void onProductDeletedDeletesAllMediaByImagePath() {
        Media first = media("img-10", "/uploads/a.png", "prod-8", FileStatus.LINKED);
        Media second = media("img-11", "/uploads/b.png", "prod-8", FileStatus.LINKED);
        when(mediaRepository.findByProductId("prod-8")).thenReturn(List.of(first, second));

        mediaStatusListener.onProductDeleted(new ProductDeletedEvent("prod-8"));

        verify(mediaRepository).findByProductId("prod-8");
        verify(mediaRepository).deleteById("/uploads/a.png");
        verify(mediaRepository).deleteById("/uploads/b.png");
    }

    @Test
    void onProductDeletedDoesNothingWhenNoMediaFound() {
        when(mediaRepository.findByProductId("prod-empty")).thenReturn(List.of());

        mediaStatusListener.onProductDeleted(new ProductDeletedEvent("prod-empty"));

        verify(mediaRepository).findByProductId("prod-empty");
        verify(mediaRepository, never()).deleteById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void onImagesLinkedHandlesRepositoryLookupFailurePerMedia() {
        when(mediaRepository.findById("img-error")).thenThrow(new RuntimeException("db error"));

        mediaStatusListener.onImagesLinked(new ImagesLinkedEvent("prod-1", List.of("img-error")));

        verify(mediaRepository).findById("img-error");
        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Media media(String id, String imagePath, String productId, FileStatus status) {
        return Media.builder()
                .id(id)
                .imagePath(imagePath)
                .productId(productId)
                .userId("user-1")
                .status(status)
                .createdAt(Instant.parse("2026-03-27T10:00:00Z"))
                .build();
    }
}
