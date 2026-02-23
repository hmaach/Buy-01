package com.buy01.product.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private float price;
    private String userId;
    private List<String> mediaIds;
    private Instant createdAt;
    private Instant updatedAt;
}