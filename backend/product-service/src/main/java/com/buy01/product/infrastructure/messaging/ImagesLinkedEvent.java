package com.buy01.product.infrastructure.messaging;

import java.util.List;

public record ImagesLinkedEvent(
        String productId,
        List<String> mediaIds) {
}