package com.buy01.media.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.buy01.media.domain.model.FileStatus;
import com.buy01.media.domain.model.Media;
import com.buy01.media.domain.ports.outbound.MediaRepositoryPort;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MongoMediaAdapter implements MediaRepositoryPort {
    private final SpringDataProductRepository mediaRepository;

    @Override
    public Media save(Media media) {
        var savedMedia = mediaRepository.save(MediaDocumentMapper.toDocument(media));
        return MediaDocumentMapper.toDomain(savedMedia);
    }

    @Override
    public Optional<Media> findById(String id) {
        var media = mediaRepository.findById(id);
        return media.map(MediaDocumentMapper::toDomain);
    }

    @Override
    public List<Media> findByStatusAndCreatedAtBefore(FileStatus status, Instant threshold) {
        var medias = mediaRepository.findByStatusAndCreatedAtBefore(status, threshold);
        return medias.stream().map(MediaDocumentMapper::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mediaRepository.deleteById(id);
    }

}
