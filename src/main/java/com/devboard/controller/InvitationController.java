package com.devboard.controller;

import com.devboard.dto.InvitationResponse;
import com.devboard.dto.InviteRequest;
import com.devboard.entity.User;
import com.devboard.repository.UserRepository;
import com.devboard.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserRepository userRepository;

    public InvitationController(InvitationService invitationService,
                                UserRepository userRepository) {
        this.invitationService = invitationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/projects/{projectId}/invitations")
    public ResponseEntity<InvitationResponse> invite(
            @PathVariable Long projectId,
            @Valid @RequestBody InviteRequest request,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.invite(projectId, request, userId));
    }

    @GetMapping("/projects/{projectId}/invitations")
    public ResponseEntity<List<InvitationResponse>> listForProject(
            @PathVariable Long projectId,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(invitationService.listForProject(projectId, userId));
    }

    @GetMapping("/invitations/pending")
    public ResponseEntity<List<InvitationResponse>> pendingForMe(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(invitationService.listPendingForEmail(me.getEmail()));
    }

    @PostMapping("/invitations/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        invitationService.accept(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/{id}/decline")
    public ResponseEntity<Void> decline(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        invitationService.decline(id, userId);
        return ResponseEntity.noContent().build();
    }
}