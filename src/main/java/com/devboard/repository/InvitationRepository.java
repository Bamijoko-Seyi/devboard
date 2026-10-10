package com.devboard.repository;

import com.devboard.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findAllByInviteeEmailAndStatus(String email, Invitation.Status status);

    Optional<Invitation> findByProjectIdAndInviteeEmailAndStatus(
            Long projectId, String email, Invitation.Status status);

    List<Invitation> findAllByProjectId(Long projectId);
}