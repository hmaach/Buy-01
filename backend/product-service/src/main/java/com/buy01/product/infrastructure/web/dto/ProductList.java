package com.buy01.product.infrastructure.web.dto;

import java.time.Instant;

public class ProductList {
    private String id;
    private String name;
    private double price;
    private String image;
    private Instant createdAt;

    public ProductList(String id, String name, double price, Instant createdAt, String image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
