package com.buy01.product.application.web.dto;

import java.util.List;

public record ProductCreatedEvent(
        String productIdm,
        List<String> mediaIdsm,
        String sellerIdm) {
}
