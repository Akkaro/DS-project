package com.example.demo.handlers;

import com.example.demo.handlers.exceptions.model.CustomException;
import com.example.demo.handlers.exceptions.model.ExceptionHandlerResponseDTO;
import com.example.demo.handlers.exceptions.model.TokenRefreshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        var body = new ExceptionHandlerResponseDTO(
                "Validation failed",
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                MethodArgumentNotValidException.class.getSimpleName(),
                details,
                request.getDescription(false)
        );

        return handleExceptionInternal(ex, body, headers, httpStatus, request);
    }

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<Object> handleCustomExceptions(CustomException ex, WebRequest request) {
        var body = new ExceptionHandlerResponseDTO(
                ex.getResource(),
                ex.getStatus().getReasonPhrase(),
                ex.getStatus().value(),
                ex.getMessage(),
                ex.getValidationErrors(),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, new HttpHeaders(), ex.getStatus(), request);
    }

    @ExceptionHandler(TokenRefreshException.class)
    protected ResponseEntity<Object> handleTokenRefreshException(TokenRefreshException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        var body = new ExceptionHandlerResponseDTO(
                "Token refresh",
                status.getReasonPhrase(),
                status.value(),
                ex.getMessage(),
                List.of(),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(WebClientResponseException.class)
    protected ResponseEntity<Object> handleWebClientResponseException(WebClientResponseException ex, WebRequest request) {
        log.error("WebClient Error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        var body = new ExceptionHandlerResponseDTO(
                "Upstream service error",
                status.getReasonPhrase(),
                status.value(),
                ex.getMessage(),
                List.of(ex.getResponseBodyAsString()),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        var body = new ExceptionHandlerResponseDTO(
                "Unexpected error",
                status.getReasonPhrase(),
                status.value(),
                ex.getClass().getSimpleName(),
                List.of("An unexpected error occurred"),
                request.getDescription(false)
        );
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
}