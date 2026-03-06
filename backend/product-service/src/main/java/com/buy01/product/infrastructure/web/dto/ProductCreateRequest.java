package com.buy01.product.infrastructure.web.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
        @NotBlank(message = "Product name is required") @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters") String name,

        @NotBlank(message = "Product description is required") @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters") String description,

        @Min(value = 1, message = "Price must be at least 1.0") @Max(value = 10000, message = "Price cannot exceed 10,000.0") double price,

        @Min(value = 0, message = "Quantity cannot be negative") @Max(value = 1000, message = "Quantity cannot exceed 1,000 units") int quantity,

        @NotEmpty(message = "At least one media ID is required") @Size(min = 1, max = 10, message = "You can upload between 1 and 10 images") List<String> imagesIds) {
}
