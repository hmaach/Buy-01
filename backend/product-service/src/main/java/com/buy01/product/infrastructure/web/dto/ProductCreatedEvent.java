package com.buy01.product.infrastructure.web.dto;

import java.util.List;

public record ProductCreatedEvent(
        String productIdm,
        List<String> mediaIdsm,
        String sellerIdm) {
}
