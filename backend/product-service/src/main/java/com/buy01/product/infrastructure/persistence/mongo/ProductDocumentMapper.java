package com.buy01.product.infrastructure.persistence.mongo;

import org.springframework.stereotype.Component;

import com.buy01.product.domain.model.Product;

@Component
public class ProductDocumentMapper {

    public ProductDocument toDocument(Product p) {
        ProductDocument d = new ProductDocument();
        d.setId(p.getId());
        d.setName(p.getName());
        d.setDescription(p.getDescription());
        d.setPrice(p.getPrice());
        d.setUserId(p.getUserId());
        d.setCreatedAt(p.getCreatedAt());
        d.setUpdatedAt(p.getUpdatedAt());
        return d;
    }

    public Product toDomain(ProductDocument d) {
        return Product.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .price(d.getPrice())
                .userId(d.getUserId())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}