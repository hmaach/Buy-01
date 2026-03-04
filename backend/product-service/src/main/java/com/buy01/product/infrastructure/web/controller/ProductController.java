package com.buy01.product.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.infrastructure.web.dto.ProductCreateRequest;
import com.buy01.product.infrastructure.web.dto.ProductResponse;
import com.buy01.product.infrastructure.web.mapper.ProductWebMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductWebMapper mapper;
    private final ProductUseCase productUseCase;

    String userId = "qwertyuiopasdfdfghhjfghjfh";

    @GetMapping()
    public String test() {
        return "Product service is working";
    }

    @PostMapping
    // @PreAuthorize("hasRole('SELLER')")
    public Mono<ResponseEntity<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        Product product = mapper.toDomain(request, userId);
        return productUseCase.createProduct(product, request.imagesIds())
                .map(mapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('SELLER')")
    public Mono<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductCreateRequest request) {

        Product update = mapper.toDomain(request, userId);
        return productUseCase.updateProduct(update, id, userId)
                .map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> getProduct(@PathVariable String id) {
        return productUseCase.getProductWithImages(id)
                .<ResponseEntity<?>>map(product -> ResponseEntity.ok(product))
                .switchIfEmpty(Mono.defer(() -> {
                    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                            HttpStatus.NOT_FOUND,
                            "Product with ID " + id + " not found");

                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem));
                }));
    }
}
