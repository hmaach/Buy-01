package com.buy01.product.application.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final WebClient mediaWebClient;

    public Mono<List<String>> getProductImages(String productId) {
        return mediaWebClient.get()
                .uri("/media/product/{id}", productId)
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .onErrorReturn(Collections.emptyList());
    }

    public Mono<Map<String, String>> getImagesBatch(Collection<String> ids) {
        if (ids.isEmpty())
            return Mono.just(Map.of());

        return mediaWebClient.post()
                .uri("/images/batch") // TODO: containue work from here
                .bodyValue(ids)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .onErrorReturn(Map.of());
    }
}
