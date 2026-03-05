package com.buy01.product.infrastructure.web.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MethodNotAllowedException;

import com.buy01.product.infrastructure.web.exception.Errors.MediaServiceException;
import com.nimbusds.jwt.proc.ExpiredJWTException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application using Problem Details (RFC
 * 7807).
 * <p>
 * Centralizes exception handling to return consistent, machine-readable error
 * responses.
 * Uses two main strategies:
 * <ul>
 * <li><b>TEMPLATES map</b>: predefined mappings for common exceptions → fixed
 * HTTP status + title + detail generator</li>
 * <li><b>Specific @ExceptionHandler methods</b>: for more complex/external
 * exceptions (e.g. MediaServiceException)</li>
 * </ul>
 * </p>
 *
 * <h3>How TEMPLATES works</h3>
 * 
 * <pre>
 * TEMPLATES is a static, immutable Map&lt;Class&lt;? extends Throwable&gt;, ProblemTemplate&gt;
 * It contains predefined rules for well-known exceptions.
 *
 * Each entry maps:
 *   - Exception type → ProblemTemplate
 *   - ProblemTemplate contains:
 *       - status      (HttpStatus)
 *       - title       (short human-readable summary)
 *       - detailGenerator (function that extracts meaningful detail from the exception)
 *
 * When an exception arrives in handleGeneralException():
 *   → Look up its exact class in TEMPLATES
 *   → If found → use that template
 *   → If not found → fall back to DEFAULT_TEMPLATE (500 + exception message)
 * </pre>
 *
 * <h3>External service handling (MediaServiceException)</h3>
 * <p>
 * Exceptions coming from external services (like Media Service) are handled in
 * a dedicated method:
 * {@link #handleMediaServiceException(MediaServiceException)}
 *
 * Reasons for separate handling:
 * <ul>
 * <li>Dynamic HTTP status (uses the original statusCode from the external call
 * – 400, 502, 503…)</li>
 * <li>Rich detail: includes original response body from the external
 * service</li>
 * <li>Contextual title: "Media Service Client Error", "Media Service Server
 * Error", etc.</li>
 * <li>Extra properties in response (externalStatus, externalDetail)</li>
 * <li>Different logging level (warn for 4xx, error for 5xx/network
 * failures)</li>
 * </ul>
 *
 * This separation keeps the general template-based logic clean while allowing
 * fine-grained control over external dependency failures.
 * </p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final Map<Class<? extends Throwable>, ProblemTemplate> TEMPLATES = new ProblemTemplate.Registry()
            .add(SignatureException.class, UNAUTHORIZED, "Invalid JWT Signature")
            .add(ExpiredJWTException.class, UNAUTHORIZED, "JWT Token Expired")
            .add(AuthorizationDeniedException.class, FORBIDDEN, "Access Denied")
            .add(NoResourceFoundException.class, BAD_REQUEST, "Missing Argument")
            .add(HttpMessageNotReadableException.class, BAD_REQUEST, "JSON Not Valid")
            .add(IllegalArgumentException.class, BAD_REQUEST, "Bad Argument")
            .add(NotFoundException.class, NOT_FOUND, "Validation Error")
            .add(UsernameNotFoundException.class, NOT_FOUND, "User Not Found")
            .add(MethodNotAllowedException.class, METHOD_NOT_ALLOWED, "Method Not Allowed")
            .add(WebExchangeBindException.class, BAD_REQUEST, "Validation Failed",
                    ex -> ((WebExchangeBindException) ex).getBindingResult().getFieldErrors()
                            .stream()
                            .map(fe -> fe.getField() + ": "
                                    + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : fe.toString()))
                            .collect(Collectors.joining("; ")))
            .build();

    private static final ProblemTemplate DEFAULT_TEMPLATE = new ProblemTemplate(
            INTERNAL_SERVER_ERROR,
            Throwable::getMessage,
            "Internal Server Error");

    @ExceptionHandler(MediaServiceException.class)
    public Mono<ProblemDetail> handleMediaServiceException(MediaServiceException ex) {
        HttpStatus status = (ex.getStatusCode() >= 100 && ex.getStatusCode() < 600)
                ? HttpStatus.valueOf(ex.getStatusCode())
                : INTERNAL_SERVER_ERROR;

        String title;
        if (status.is4xxClientError()) {
            title = "Media Service Client Error";
        } else if (status.is5xxServerError()) {
            title = "Media Service Server Error";
        } else {
            title = "Media Service Unavailable";
        }

        String detail = ex.getMessage();
        if (ex.getResponseBody() != null && !ex.getResponseBody().isBlank()) {
            detail += " → " + ex.getResponseBody().trim();
        }

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);

        Map<String, Object> properties = new HashMap<>();
        properties.put("externalService", "media");
        properties.put("externalStatus", ex.getStatusCode());
        if (ex.getResponseBody() != null) {
            properties.put("externalDetail", ex.getResponseBody());
        }
        pd.setProperties(properties);

        if (status.is5xxServerError() || ex.getStatusCode() == 0) {
            log.error("Media service failure: status={}, detail={}", ex.getStatusCode(), detail, ex);
        } else {
            log.warn("Media service client issue: status={}, detail={}", ex.getStatusCode(), detail);
        }

        return Mono.just(pd);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ProblemDetail> handle(Exception ex) {
        ProblemTemplate template = TEMPLATES.get(ex.getClass());
        if (template == null) {
            log.warn("Exception not handled: {}", ex.getClass().getSimpleName(), ex);
            template = DEFAULT_TEMPLATE;
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                template.status(),
                template.detailGenerator().apply(ex));
        pd.setTitle(template.title());
        return Mono.just(pd);
    }
}

record ProblemTemplate(
        HttpStatus status,
        DetailGenerator detailGenerator,
        String title) {

    public static class Registry {
        private final Map<Class<? extends Throwable>, ProblemTemplate> map = new HashMap<>();

        public Registry add(Class<? extends Throwable> type, HttpStatus status, String title) {
            map.put(type, new ProblemTemplate(status, Throwable::getMessage, title));
            return this;
        }

        public Registry add(Class<? extends Throwable> type, HttpStatus status, String title,
                DetailGenerator gen) {
            map.put(type, new ProblemTemplate(status, gen, title));
            return this;
        }

        public Map<Class<? extends Throwable>, ProblemTemplate> build() {
            return Map.copyOf(map);
        }
    }
}

@FunctionalInterface
interface DetailGenerator {
    String apply(Throwable ex);
}