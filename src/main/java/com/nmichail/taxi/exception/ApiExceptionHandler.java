package com.nmichail.taxi.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        int status = e.getStatusCode().value();
        String message = e.getReason();
        if (message == null || message.isBlank()) {
            message = e.getMessage() != null ? e.getMessage() : "Error";
        }
        ErrorResponse body = new ErrorResponse(Instant.now(), status, message);
        return ResponseEntity.status(e.getStatusCode()).body(body);
    }
}