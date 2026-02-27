package com.buy01.media.infrastructure.web.mapper;


import com.buy01.media.domain.model.Media;
import com.buy01.media.infrastructure.web.dto.MediaResponse;

public class MediaMapper {

    public static MediaResponse toResponse(Media media){
        return new MediaResponse(media.getId(), media.getStatus());
    }
}
