package com.buy01.media.infrastructure.persistence;

import com.buy01.media.domain.model.Media;

public class MediaDocumentMapper {

    public static MediaDocument toDocument(Media m) {
        var d = new MediaDocument();
        d.setImagePath(m.getImagePath());
        d.setCreatedAt(m.getCreatedAt());
        d.setStatus(m.getStatus());
        return d;
    }

    public static Media toDomain(MediaDocument d) {
        return Media.builder()
                .id(d.getId())
                .imagePath(d.getImagePath())
                .createdAt(d.getCreatedAt())
                .status(d.getStatus())
                .build();

    }
}
