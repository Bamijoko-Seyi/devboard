package com.devboard.service;

import com.devboard.dto.ProjectMemberResponse;
import com.devboard.dto.ProjectRequest;
import com.devboard.dto.ProjectResponse;
import com.devboard.entity.Project;
import com.devboard.entity.ProjectMember;
import com.devboard.entity.User;
import com.devboard.repository.ProjectMemberRepository;
import com.devboard.repository.ProjectRepository;
import com.devboard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService access;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository,
                          UserRepository userRepository,
                          ProjectAccessService access) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.access = access;
    }

    /** Lists all projects the user is a member of. */
    public List<ProjectResponse> listForUser(Long userId) {
        return memberRepository.findAllByUserId(userId).stream()
                .map(m -> toResponse(m.getProject(), m.getRole()))
                .toList();
    }

    public ProjectResponse getById(Long projectId, Long userId) {
        ProjectMember membership = access.requireMember(projectId, userId);
        return toResponse(membership.getProject(), membership.getRole());
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        project = projectRepository.save(project);

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(owner);
        member.setRole(ProjectMember.Role.OWNER);
        memberRepository.save(member);

        return toResponse(project, ProjectMember.Role.OWNER);
    }

    @Transactional
    public ProjectResponse update(Long projectId, ProjectRequest request, Long userId) {
        ProjectMember membership = access.requireEditor(projectId, userId);
        Project project = membership.getProject();

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        return toResponse(projectRepository.save(project), membership.getRole());
    }

    @Transactional
    public void delete(Long projectId, Long userId) {
        ProjectMember membership = access.requireOwner(projectId, userId);
        memberRepository.deleteAll(memberRepository.findAllByProjectId(projectId));
        projectRepository.delete(membership.getProject());
    }

    /** Member list for a project. Visible to any member. */
    public List<ProjectMemberResponse> listMembers(Long projectId, Long userId) {
        access.requireMember(projectId, userId);
        return memberRepository.findAllByProjectId(projectId).stream()
                .map(m -> new ProjectMemberResponse(
                        m.getUser().getId(),
                        m.getUser().getDisplayName(),
                        m.getUser().getEmail(),
                        m.getRole().name(),
                        m.getJoinedAt()))
                .toList();
    }

    private ProjectResponse toResponse(Project p, ProjectMember.Role role) {
        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getOwner().getId(),
                p.getOwner().getDisplayName(),
                role.name(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}