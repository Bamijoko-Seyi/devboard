package com.devboard.service;

public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException(String action) {
        super("You do not have permission to " + action);
    }
}