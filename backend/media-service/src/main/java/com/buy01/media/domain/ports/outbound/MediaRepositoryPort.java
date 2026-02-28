package com.buy01.media.domain.ports.outbound;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.buy01.media.domain.model.FileStatus;
import com.buy01.media.domain.model.Media;

public interface MediaRepositoryPort {
        Media save(Media media);

        Optional<Media> findById(String id);

        List<Media> findByStatusAndCreatedAtBefore(FileStatus status, Instant threshold);

        void deleteById(String id);
}
