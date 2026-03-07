package com.buy01.gateway.filter;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements WebFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${gateway.rate-limit.capacity:100}")
    private int capacity;

    @Value("${gateway.rate-limit.refill-minutes:1}")
    private int refillDurationMinutes;

    @Value("${gateway.rate-limit.enabled:true}")
    private boolean enabled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (!enabled) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);

        Bucket bucket = resolveBucket(clientIp);

        if (bucket.tryConsume(1)) {

            long availableTokens = bucket.getAvailableTokens();

            exchange.getResponse().getHeaders()
                    .add("X-RateLimit-Limit", String.valueOf(capacity));
            exchange.getResponse().getHeaders()
                    .add("X-RateLimit-Remaining", String.valueOf(availableTokens));

            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(capacity));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
        exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(refillDurationMinutes * 60));

        String path = exchange.getRequest().getURI().getPath();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later.");
        problemDetail.setTitle("Too Many Requests");
        problemDetail.setInstance(URI.create(path));

        String body = String.format("""
        {
            "detail": "Too many requests. Please try again later.",
            "instance": "%s",
            "status": 429,
            "title": "Too Many Requests"
        }
        """, path);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }

    private Bucket resolveBucket(String clientIp) {
        return cache.computeIfAbsent(clientIp, k -> createBucket());
    }

    private Bucket createBucket() {

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, Duration.ofMinutes(refillDurationMinutes))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIp(ServerHttpRequest request) {

        String xfHeader = request.getHeaders().getFirst("X-Forwarded-For");

        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        var remote = request.getRemoteAddress();

        if (remote != null && remote.getAddress() != null) {
            return remote.getAddress().getHostAddress();
        }

        return "unknown";
    }
}
