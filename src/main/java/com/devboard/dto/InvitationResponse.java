package com.devboard.dto;

import java.time.LocalDateTime;

public class InvitationResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String inviterDisplayName;
    private String inviteeEmail;
    private String status;
    private LocalDateTime createdAt;

    public InvitationResponse(Long id, Long projectId, String projectName,
                              String inviterDisplayName, String inviteeEmail,
                              String status, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.inviterDisplayName = inviterDisplayName;
        this.inviteeEmail = inviteeEmail;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public String getInviterDisplayName() { return inviterDisplayName; }
    public String getInviteeEmail() { return inviteeEmail; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}