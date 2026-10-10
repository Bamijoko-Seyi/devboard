package com.devboard.service;

import com.devboard.entity.ProjectMember;
import com.devboard.repository.ProjectMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjectAccessService {

    private final ProjectMemberRepository memberRepository;

    public ProjectAccessService(ProjectMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /** Throws if the user is not a member of the project at all. */
    public ProjectMember requireMember(Long projectId, Long userId) {
        return memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(NotProjectMemberException::new);
    }

    /** Requires at least EDITOR (EDITOR or OWNER). */
    public ProjectMember requireEditor(Long projectId, Long userId) {
        ProjectMember m = requireMember(projectId, userId);
        if (m.getRole() == ProjectMember.Role.VIEWER) {
            throw new InsufficientPermissionException("modify this project");
        }
        return m;
    }

    /** Requires OWNER. */
    public ProjectMember requireOwner(Long projectId, Long userId) {
        ProjectMember m = requireMember(projectId, userId);
        if (m.getRole() != ProjectMember.Role.OWNER) {
            throw new InsufficientPermissionException("manage members of this project");
        }
        return m;
    }
}