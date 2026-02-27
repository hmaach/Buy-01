package com.buy01.media.infrastructure.web.dto;


import com.buy01.media.domain.model.FileStatus;

public record MediaResponse(
                String imagesId,
                FileStatus status) {
}
