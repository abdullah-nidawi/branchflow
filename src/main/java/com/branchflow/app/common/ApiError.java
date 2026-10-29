package com.branchflow.app.common;

import java.time.Instant;
import java.util.Map;

/** Stable error shape shared by validation, domain, and authorization failures. */
public record ApiError(int status, String error, String message, Instant timestamp, Map<String, String> details) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Instant.now(), Map.of());
    }
}
