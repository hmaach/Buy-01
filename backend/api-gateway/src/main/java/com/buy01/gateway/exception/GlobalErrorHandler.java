package com.buy01.gateway.exception;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Determine appropriate HTTP status
        HttpStatus status = determineHttpStatus(ex);
        String title = determineTitle(ex);
        String detail = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";

        // Get the request path for instance
        String path = exchange.getRequest().getPath().value();

        // Create ProblemDetail response
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(path));

        // Set response headers
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Serialize ProblemDetail to JSON
        String errorMessage = serializeProblemDetail(problemDetail);
        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof org.springframework.web.reactive.resource.NoResourceFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineTitle(Throwable ex) {
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            return "Forbidden";
        }
        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            return "Unauthorized";
        }
        if (ex instanceof IllegalArgumentException) {
            return "Bad Request";
        }
        if (ex instanceof org.springframework.web.reactive.resource.NoResourceFoundException) {
            return "Not Found";
        }
        
        return "Internal Server Error";
    }

    private String serializeProblemDetail(ProblemDetail problemDetail) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"detail\":\"").append(escapeJson(problemDetail.getDetail())).append("\",");
        sb.append("\"instance\":\"").append(escapeJson(problemDetail.getInstance() != null ? problemDetail.getInstance().toString() : "")).append("\",");
        sb.append("\"status\":").append(problemDetail.getStatus()).append(",");
        sb.append("\"title\":\"").append(escapeJson(problemDetail.getTitle())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
