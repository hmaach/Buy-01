package com.buy01.product.application.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.buy01.product.application.web.dto.ProductCreateRequest;
import com.buy01.product.application.web.dto.ProductResponse;
import com.buy01.product.application.web.mapper.ProductWebMapper;
import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.inbound.ProductUseCase;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductWebMapper mapper;
    private final ProductUseCase productUseCase;

    public ProductController(ProductWebMapper mapper, ProductUseCase productUseCase) {
        this.mapper = mapper;
        this.productUseCase = productUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Mono<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Authentication auth) {

        String userId = auth.getName();
        Product product = mapper.toDomain(request, userId);
        Mono<ProductResponse> res = productUseCase.createProduct(product)
                .map(mapper::toResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
