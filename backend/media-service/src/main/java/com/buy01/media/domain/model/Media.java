package com.buy01.media.domain.model;

import java.time.Instant;

public class Media {

    private final String id;
    private final String imagePath;
    private final String productId;
    private final FileStatus status;
    private final Instant createdAt;

    
    private Media(Builder builder) {
        this.id = builder.id;
        this.imagePath = builder.imagePath;
        this.productId = builder.productId;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getProductId() {
        return productId;
    }

    public FileStatus getStatus() {
        return status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private String id;
        private String imagePath;
        private String productId;
        private FileStatus status;
        private Instant createdAt;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder status(FileStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public Media build() {
            // if (imagePath == null || imagePath.trim().isEmpty()) {
            // throw new IllegalStateException("imagePath is required");
            // }
            // if (productId == null || productId.trim().isEmpty()) {
            // throw new IllegalStateException("productId is required");
            // }
            // if (status == null) {
            // throw new IllegalStateException("status is required");
            // }
            return new Media(this);
        }
    }

    @Override
    public String toString() {
        return "Media{" +
                "id='" + id + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", productId='" + productId + '\'' +
                ", status=" + status +
                '}';
    }
}
