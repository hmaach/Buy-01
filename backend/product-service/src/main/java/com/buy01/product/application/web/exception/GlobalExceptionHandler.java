package com.buy01.product.application.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler(ProductNotFoundException.class)
    // public Mono<ResponseEntity<ProblemDetail>> handleNotFound(
    //         ProductNotFoundException ex,
    //         ServerWebExchange exchange) {

    //     ProblemDetail problem = ProblemDetail.forStatusAndDetail(
    //             HttpStatus.NOT_FOUND,
    //             ex.getMessage()
    //     );
    //     problem.setTitle("Product Not Found");
    //     problem.setType(URI.create("https://api.example.com/errors/not-found"));
    //     problem.setProperty("timestamp", Instant.now());
    //     problem.setProperty("path", exchange.getRequest().getPath().value());

    //     return Mono.just(ResponseEntity
    //             .status(HttpStatus.NOT_FOUND)
    //             .body(problem));
    // }

    // @ExceptionHandler(ProductOwnershipException.class)
    // public Mono<ResponseEntity<ProblemDetail>> handleOwnershipViolation(
    //         ProductOwnershipException ex,
    //         ServerWebExchange exchange) {

    //     ProblemDetail problem = ProblemDetail.forStatusAndDetail(
    //             HttpStatus.FORBIDDEN,
    //             ex.getMessage()
    //     );
    //     problem.setTitle("Ownership Violation");
    //     problem.setType(URI.create("https://api.example.com/errors/forbidden"));

    //     return Mono.just(ResponseEntity
    //             .status(HttpStatus.FORBIDDEN)
    //             .body(problem));
    // }

    // 2. Handle validation errors (e.g. @Valid in controller)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationErrors(
            WebExchangeBindException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("title", "Validation Failed");
        body.put("timestamp", Instant.now());

        List<Map<String, String>> errors = ex.getFieldErrors().stream()
                .map(err -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("field", err.getField());
                    error.put("message", err.getDefaultMessage());
                    return error;
                })
                .toList();

        body.put("errors", errors);

        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleGeneric(
            Exception ex,
            ServerWebExchange exchange) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        problem.setProperty("info", ex.getMessage());
        problem.setProperty("exception", ex.getClass());
        problem.setTitle("Internal Server Error");
        System.out.println(ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem));
    }
}