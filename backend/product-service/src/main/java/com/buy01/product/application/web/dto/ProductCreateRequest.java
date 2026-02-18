package com.buy01.product.application.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductCreateRequest(
        @NotBlank String name,
        String description,
        @Min(1) BigDecimal price) {
}
