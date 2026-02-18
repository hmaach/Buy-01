package com.buy01.product.application.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String userId;
    // private List<String> imageUrls;
    private Instant createdAt;
    private Instant updatedAt;
}