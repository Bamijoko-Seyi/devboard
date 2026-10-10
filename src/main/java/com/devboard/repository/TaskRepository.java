package com.devboard.repository;

import com.devboard.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Task> findAllByProjectIdAndStatus(Long projectId, Task.Status status);
}