package com.buy01.product.domain.ports.inbound;

import java.time.Instant;
import java.util.List;

import com.buy01.product.domain.model.Product;
import com.buy01.product.infrastructure.web.dto.ProductList;
import com.buy01.product.infrastructure.web.dto.ProductResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductUseCase {
    Mono<Product> createProduct(Product product, List<String> imagesids);

    Mono<Product> updateProduct(Product update, String id, String userId);

    Mono<ProductResponse> getProductWithImages(String productId);

    Flux<ProductList> getProductsList(Instant beforeTime);
}
