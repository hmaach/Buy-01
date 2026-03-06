package com.buy01.gateway.filter;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
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

            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(capacity));
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(availableTokens));

            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(capacity));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
        exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(refillDurationMinutes * 60));

        byte[] bytes = """
        {
            "status":429,
            "error":"TOO_MANY_REQUESTS",
            "message":"Too many requests. Please try again later."
        }
        """.getBytes();

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }

    private Bucket resolveBucket(String clientIp) {
        return cache.computeIfAbsent(clientIp, k -> createBucket());
    }

    private Bucket createBucket() {

        Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.greedy(capacity, Duration.ofMinutes(refillDurationMinutes)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIp(ServerHttpRequest request) {

        String xfHeader = request.getHeaders().getFirst("X-Forwarded-For");

        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }
}
