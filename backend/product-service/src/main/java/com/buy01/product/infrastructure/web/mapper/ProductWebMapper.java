package com.buy01.product.infrastructure.web.mapper;

import org.springframework.stereotype.Component;

import com.buy01.product.domain.model.Product;
import com.buy01.product.infrastructure.web.dto.ProductCreateRequest;
import com.buy01.product.infrastructure.web.dto.ProductResponse;

@Component
public class ProductWebMapper {

    public Product toDomain(ProductCreateRequest request, String userId) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .mediaIds(request.mediaIds())
                .userId(userId)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .userId(product.getUserId())
                .mediaIds(product.getMediaIds())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}