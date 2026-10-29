package com.branchflow.app.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException exception) {
        return new ApiError(
                404,
                exception.getMessage(),
                Instant.now());
    }
}