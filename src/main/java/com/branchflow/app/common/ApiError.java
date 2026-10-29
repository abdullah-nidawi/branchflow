package com.branchflow.app.common;

import java.time.Instant;

public record ApiError(
        int status,
        String msg,
        Instant timestamp) {
}
