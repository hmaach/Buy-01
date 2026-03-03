package com.buy01.product.infrastructure.web.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProductCreateRequest(
        @NotBlank(message = "Product name is required") 
        String name,
        
        @NotBlank(message = "Product description is required") 
        String description,
        
        @Min(value = 1, message = "Price must be at least 1.0")
        float price,

        @NotNull(message = "quantity is required")
        int quantity,
        
        @NotEmpty(message = "At least one media ID is required")
        List<String> imagesIds) {
}
