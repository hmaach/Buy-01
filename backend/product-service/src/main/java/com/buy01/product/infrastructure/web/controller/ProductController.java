package com.buy01.product.infrastructure.web.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.infrastructure.security.JwtAuthenticationFilter.UserPrincipal;
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

    @GetMapping("/user")
    public UserPrincipal testUser(Authentication authentication) {
        UserPrincipal currUser = (UserPrincipal) authentication.getPrincipal();
        return currUser;
    }

    @PostMapping
    // @PreAuthorize("hasRole('SELLER')")
    public Mono<ResponseEntity<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Product product = mapper.toDomain(request, principal.id());
        return productUseCase.createProduct(product, request.imagesIds())
                .map(mapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PatchMapping("/{id}")
    // @PreAuthorize("hasRole('SELLER')")
    public Mono<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        Product update = mapper.toDomain(request, principal.id());
        return productUseCase.updateProduct(update, id, principal.id())
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

    @GetMapping
    public ResponseEntity<?> getProductsList(@RequestParam(required = false) Instant beforeTime) {
        if (beforeTime == null) {
            beforeTime = Instant.now();
        }
        var prodctList = productUseCase.getProductsList(beforeTime);
        return ResponseEntity.ok(prodctList);
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('SELLER')")
    public Mono<Void> deleteProduct(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {

        productUseCase.deleteProduct(id, principal.id());
        
        return Mono.just(null);
    }
}
