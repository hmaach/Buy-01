package com.buy01.product.infrastructure.web.dto;

import java.util.List;

public record ImageLinkedEvent(
        String productId,
        List<String> mediaIds) {
}