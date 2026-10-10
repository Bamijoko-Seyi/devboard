package com.devboard.service;

public class NotProjectMemberException extends RuntimeException {
    public NotProjectMemberException() {
        super("You do not have access to this project");
    }
}