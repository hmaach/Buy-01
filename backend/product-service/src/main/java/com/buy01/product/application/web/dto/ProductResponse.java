package com.buy01.product.application.web.dto;

import java.time.Instant;
import java.util.List;

import com.buy01.product.domain.model.PositiveFloat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private PositiveFloat price;
    private String userId;
    private List<String> mediaIds;
    private Instant createdAt;
    private Instant updatedAt;
}