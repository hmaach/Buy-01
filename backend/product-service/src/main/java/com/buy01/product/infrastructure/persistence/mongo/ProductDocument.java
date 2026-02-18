package com.buy01.product.infrastructure.persistence.mongo;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "products")
public class ProductDocument {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    @Field("user_id")
    private String userId;
    // private List<String> imageUrls = new ArrayList<>();
    @CreatedDate
    @Field("created_at")
    private Instant createdAt;
    @Field("updated_at")
    @LastModifiedDate
    private Instant updatedAt;
}
