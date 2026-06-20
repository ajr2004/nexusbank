package com.nexusbank.loan_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException; // ⚡ ADDED
import feign.FeignException; // ⚡ ADDED

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔍 1. Catch-All Interceptor for Network Exceptions coming from other Microservices (like banking-service)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignStatusException(FeignException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.status() != -1 ? ex.status() : 500);
        body.put("error", "Downstream Service Communication Failure");
        
        // This extracts the exact JSON error text sent back by the banking service!
        body.put("message", ex.contentUTF8().isEmpty() ? ex.getMessage() : ex.contentUTF8());

        HttpStatus status = HttpStatus.resolve(ex.status());
        return new ResponseEntity<>(body, status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 🔍 2. Explicitly catches missing entities/records and responds with a true 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(EntityNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Loan Resource Missing");

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 🔍 3. Explicitly catches strict validation or calculation business rule violations
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Loan Business Rule Violation");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ✅ Runtime Exceptions (Fallback)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("status", 400);
        body.put("error", "Loan Operation Failed");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ✅ Validation Exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("validationErrors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ✅ Generic Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Something went wrong");
        body.put("status", 500);
        body.put("error", "Internal Server Error");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}