package com.urlshortner.url_shortner.controller;

import com.urlshortner.url_shortner.dto.ErrorResponse;
import com.urlshortner.url_shortner.service.UrlExpiredException;
import com.urlshortner.url_shortner.service.UrlNotFoundException;
import com.urlshortner.url_shortner.service.InvalidUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({InvalidUrlException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException validationException) {
            String message = validationException.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse("Request validation failed");
            return ResponseEntity.badRequest().body(new ErrorResponse(message));
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest() {
        return ResponseEntity.badRequest().body(new ErrorResponse("Request body must be valid JSON"));
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFound(UrlNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpired(UrlExpiredException exception) {
        return ResponseEntity.status(HttpStatus.GONE).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedFailure() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred"));
    }
}
