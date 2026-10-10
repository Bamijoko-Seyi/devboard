package com.devboard.service;

import com.devboard.dto.InvitationResponse;
import com.devboard.dto.InviteRequest;
import com.devboard.entity.Invitation;
import com.devboard.entity.Project;
import com.devboard.entity.ProjectMember;
import com.devboard.entity.User;
import com.devboard.repository.InvitationRepository;
import com.devboard.repository.ProjectMemberRepository;
import com.devboard.repository.ProjectRepository;
import com.devboard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService access;

    public InvitationService(InvitationRepository invitationRepository,
                             ProjectRepository projectRepository,
                             ProjectMemberRepository memberRepository,
                             UserRepository userRepository,
                             ProjectAccessService access) {
        this.invitationRepository = invitationRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.access = access;
    }

    @Transactional
    public InvitationResponse invite(Long projectId, InviteRequest request, Long inviterId) {
        access.requireOwner(projectId, inviterId);
        String email = request.getEmail().toLowerCase();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If the email belongs to an existing user, check they're not already a member
        userRepository.findByEmail(email).ifPresent(existing -> {
            if (memberRepository.existsByProjectIdAndUserId(projectId, existing.getId())) {
                throw new RuntimeException("User is already a member of this project");
            }
        });

        // Reject duplicate pending invitation
        invitationRepository.findByProjectIdAndInviteeEmailAndStatus(
                projectId, email, Invitation.Status.PENDING
        ).ifPresent(existing -> {
            throw new RuntimeException("Invitation already pending for this email");
        });

        Invitation inv = new Invitation();
        inv.setProject(project);
        inv.setInviter(inviter);
        inv.setInviteeEmail(email);
        inv.setStatus(Invitation.Status.PENDING);

        return toResponse(invitationRepository.save(inv));
    }

    public List<InvitationResponse> listPendingForEmail(String email) {
        return invitationRepository
                .findAllByInviteeEmailAndStatus(email.toLowerCase(), Invitation.Status.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InvitationResponse> listForProject(Long projectId, Long userId) {
        access.requireMember(projectId, userId);
        return invitationRepository.findAllByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void accept(Long invitationId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Invitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!inv.getInviteeEmail().equalsIgnoreCase(user.getEmail())) {
            throw new InsufficientPermissionException("accept this invitation");
        }
        if (inv.getStatus() != Invitation.Status.PENDING) {
            throw new RuntimeException("Invitation already handled");
        }

        // Add as EDITOR by default
        if (!memberRepository.existsByProjectIdAndUserId(inv.getProject().getId(), userId)) {
            ProjectMember m = new ProjectMember();
            m.setProject(inv.getProject());
            m.setUser(user);
            m.setRole(ProjectMember.Role.EDITOR);
            memberRepository.save(m);
        }

        inv.setStatus(Invitation.Status.ACCEPTED);
        invitationRepository.save(inv);
    }

    @Transactional
    public void decline(Long invitationId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Invitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!inv.getInviteeEmail().equalsIgnoreCase(user.getEmail())) {
            throw new InsufficientPermissionException("decline this invitation");
        }
        if (inv.getStatus() != Invitation.Status.PENDING) {
            throw new RuntimeException("Invitation already handled");
        }

        inv.setStatus(Invitation.Status.DECLINED);
        invitationRepository.save(inv);
    }

    private InvitationResponse toResponse(Invitation i) {
        return new InvitationResponse(
                i.getId(),
                i.getProject().getId(),
                i.getProject().getName(),
                i.getInviter().getDisplayName(),
                i.getInviteeEmail(),
                i.getStatus().name(),
                i.getCreatedAt()
        );
    }
}