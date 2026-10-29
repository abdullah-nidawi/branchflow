package com.branchflow.app.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException exception) {
        return ApiError.of(404, "Not Found", exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException exception) {
        return ApiError.of(409, "Conflict", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> details = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return new ApiError(400, "Bad Request", "Request validation failed", java.time.Instant.now(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException exception) {
        return ApiError.of(400, "Bad Request", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadableBody() {
        return ApiError.of(400, "Bad Request", "Request body is missing or contains invalid JSON");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIntegrityConflict() {
        return ApiError.of(409, "Conflict", "The request conflicts with existing data");
    }
}
