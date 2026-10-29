package com.branchflow.app.common;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) {
        super(msg);
    }
}
