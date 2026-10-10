package com.devboard.dto;

import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerDisplayName;
    private String currentUserRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse(Long id, String name, String description,
                           Long ownerId, String ownerDisplayName, String currentUserRole,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerDisplayName = ownerDisplayName;
        this.currentUserRole = currentUserRole;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getOwnerId() { return ownerId; }
    public String getCurrentUserRole() { return currentUserRole; }
    public String getOwnerDisplayName() { return ownerDisplayName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}