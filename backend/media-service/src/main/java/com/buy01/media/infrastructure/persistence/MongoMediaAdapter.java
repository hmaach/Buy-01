package com.buy01.media.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

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

}
