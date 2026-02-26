package com.buy01.media.infrastructure.web.dto;

import java.util.List;

public record ImagesLinkedEvent(
        String productId,
        List<String> mediaIds) {
}