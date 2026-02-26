package com.buy01.media.domain.ports.outbound;

import java.util.Optional;

import com.buy01.media.domain.model.Media;

public interface MediaRepositoryPort {
        Media save(Media media);
        
        Optional<Media> findById(String id);
}
