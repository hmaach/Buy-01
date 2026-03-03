package com.buy01.product.domain.model;

import java.time.Instant;
import java.util.List;

public class Product {

    private final String id;
    private final String name;
    private final String description;
    private final double price;
    private final Integer quantity;
    private final String userId;
    private final List<String> imagesIds;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.quantity = builder.quantity;
        this.userId = builder.userId;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.imagesIds = builder.imagesIds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getImagesIds() {
        return imagesIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (price <= 0.0f) {
            throw new IllegalArgumentException("price must be positive.");
        }
        // TODO add more validation
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    // ────────────────────────────────────────────────────────────
    public static class Builder {
        private String id;
        private String name;
        private String description;
        private double price;
        private Integer quantity;
        private String userId;
        private List<String> imagesIds;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        private Builder(Product product) {
            this.id = product.id;
            this.name = product.name;
            this.description = product.description;
            this.price = product.price;
            this.quantity = product.quantity;
            this.userId = product.userId;
            this.createdAt = product.createdAt;
            this.updatedAt = product.updatedAt;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder imagesIds(List<String> ids) {
            this.imagesIds = ids;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Product build() {
            Product p = new Product(this);
            p.validate();
            return p;
        }
    }
}
