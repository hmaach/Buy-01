package com.buy01.product.application.web.dto;

import java.util.List;

import com.buy01.product.domain.model.PositiveFloat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductCreateRequest(
                @NotBlank String name,
                String description,
                @Min(1) PositiveFloat price,
                List<String> mediaIds) {
}
