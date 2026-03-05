package com.buy01.product.application.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.buy01.product.infrastructure.web.exception.Errors.MediaServiceException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Service responsible for fetching product images from the Media Service.
 * <p>
 * Provides both single-product and batch image retrieval with proper error
 * handling.
 * On failure, throws {@link MediaServiceException} instead of returning empty
 * results silently.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ImageService {
    private final WebClient mediaWebClient;

    public Mono<List<String>> getProductImages(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        return mediaWebClient.get()
                .uri("/media/product/{id}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("No error details")
                        .map(body -> new MediaServiceException(
                                response.statusCode().value(),
                                "Media service client error for product " + productId,
                                body)))
                .onStatus(HttpStatusCode::is5xxServerError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("No error details")
                        .map(body -> new MediaServiceException(
                                response.statusCode().value(),
                                "Media service server error for product " + productId,
                                body)))
                .bodyToFlux(String.class)
                .collectList()

                .onErrorMap(e -> !(e instanceof MediaServiceException),
                        e -> new MediaServiceException(
                                0,
                                "Failed to reach media service for product " + productId + ": " + e.getMessage(),
                                null));
    }

    public Mono<Map<String, String>> getImagesBatch(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Mono.just(Map.of());
        }
        var requestBody = Map.of("productIds", new ArrayList<>(ids));

        return mediaWebClient.post()
                .uri("/media/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()

                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("No error body")
                        .map(body -> new MediaServiceException(
                                response.statusCode().value(),
                                "Media service client error",
                                body)))
                .onStatus(HttpStatusCode::is5xxServerError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("No error body")
                        .map(body -> new MediaServiceException(
                                response.statusCode().value(),
                                "Media service server error",
                                body)))

                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })

                .onErrorMap(e -> !(e instanceof MediaServiceException),
                        e -> new MediaServiceException(0, "Failed to reach media service: " + e.getMessage(), null));
    }
}
