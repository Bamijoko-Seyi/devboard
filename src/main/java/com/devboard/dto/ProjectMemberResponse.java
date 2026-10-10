package com.devboard.dto;

import java.time.LocalDateTime;

public class ProjectMemberResponse {

    private Long userId;
    private String displayName;
    private String email;
    private String role;
    private LocalDateTime joinedAt;

    public ProjectMemberResponse(Long userId, String displayName, String email,
                                 String role, LocalDateTime joinedAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}