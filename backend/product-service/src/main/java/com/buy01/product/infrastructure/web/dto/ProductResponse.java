package com.buy01.product.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;

import com.buy01.product.domain.model.Product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String userId;
    private List<String> imagesIds;
    private Instant createdAt;
    private Instant updatedAt;

    public ProductResponse(String id, String name, String description, double price, int quantity, String userId,
            List<String> imagesIds, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
        this.imagesIds = imagesIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ProductResponse(Product product, List<String> imagesIds) {
        this.id = product.getId();
        this.name = product.getName();
        this.userId = product.getUserId();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.quantity = product.getQuantity();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        this.imagesIds = imagesIds;
    }
}