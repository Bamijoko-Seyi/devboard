package com.devboard.repository;

import com.devboard.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findAllByProjectId(Long projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMember> findAllByUserId(Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectIdAndRole(Long projectId, ProjectMember.Role role);
}