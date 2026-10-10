package com.devboard.dto;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String email;
    private String displayName;
    private LocalDateTime createdAt;

    public UserResponse(Long id, String email, String displayName, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }

    // Getters only — we don't want this class to be mutable

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}