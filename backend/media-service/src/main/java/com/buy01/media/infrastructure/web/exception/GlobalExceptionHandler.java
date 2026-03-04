package com.buy01.media.infrastructure.web.exception;

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
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.buy01.media.infrastructure.web.exception.Errors.Faileduploadedfile;
import com.buy01.media.infrastructure.web.exception.Errors.NotFound;
import com.nimbusds.jwt.proc.ExpiredJWTException;

import lombok.extern.slf4j.Slf4j;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Map<Class<? extends Throwable>, ProblemTemplate> TEMPLATES = new ProblemTemplate.Registry()
            .add(SignatureException.class, UNAUTHORIZED, "Invalid JWT Signature")
            .add(ExpiredJWTException.class, UNAUTHORIZED, "JWT Token Expired")
            .add(AuthorizationDeniedException.class, FORBIDDEN, "Access Denied")
            .add(MissingServletRequestPartException.class, BAD_REQUEST, "Missing Argument")
            .add(HttpMessageNotReadableException.class, BAD_REQUEST, "JSON Not Valid")
            .add(IllegalArgumentException.class, BAD_REQUEST, "Bad Argument")
            .add(NotFoundException.class, NOT_FOUND, "Validation Error")
            .add(UsernameNotFoundException.class, NOT_FOUND, "User Not Found")
            .add(MissingRequestValueException.class, BAD_REQUEST, "Missing Argument")
            .add(Faileduploadedfile.class, CONFLICT, "Failed to uploaded file")
            .add(NotFound.class, NOT_FOUND, "Not Found")
            .add(NoResourceFoundException.class, NOT_FOUND, "Path Not Found")
            .add(WebExchangeBindException.class, BAD_REQUEST, "Validation Failed",
                    ex -> "Fields: " + ((WebExchangeBindException) ex).getBindingResult().getFieldErrorCount())
            .build();

    private static final ProblemTemplate DEFAULT_TEMPLATE = new ProblemTemplate(
            INTERNAL_SERVER_ERROR,
            Throwable::getMessage,
            "Internal Server Error");

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        problemDetail.setTitle("Application Error");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handle(Exception ex) {
        ProblemTemplate template = TEMPLATES.get(ex.getClass());
        if (template == null) {
            System.err.println(ex + "\n");
            log.warn("Exception not handled happen: ", ex);
            template = DEFAULT_TEMPLATE;
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                template.status(),
                template.detailGenerator().apply(ex));
        pd.setTitle(template.title());
        return pd;
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