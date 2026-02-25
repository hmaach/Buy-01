package com.buy01.media.domain.ports.outbound;

import com.buy01.media.domain.model.Media;

public interface MediaRepositoryPort {
        Media save(Media media);
        
        Media findById(String id);
}
