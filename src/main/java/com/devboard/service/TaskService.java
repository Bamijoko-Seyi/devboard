package com.devboard.service;

import com.devboard.dto.TaskRequest;
import com.devboard.dto.TaskResponse;
import com.devboard.entity.Project;
import com.devboard.entity.Task;
import com.devboard.entity.User;
import com.devboard.repository.ProjectRepository;
import com.devboard.repository.TaskRepository;
import com.devboard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService access;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ProjectAccessService access) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listForProject(Long projectId, Long userId) {
        access.requireMember(projectId, userId);
        return taskRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TaskResponse create(Long projectId, TaskRequest request, Long userId) {
        access.requireEditor(projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(parseStatus(request.getStatus(), Task.Status.TODO));
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            // Only allow assignment to a project member
            access.requireMember(projectId, request.getAssigneeId());
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long projectId, Long taskId, TaskRequest request, Long userId) {
        access.requireEditor(projectId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(parseStatus(request.getStatus(), task.getStatus()));
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() == null) {
            task.setAssignee(null);
        } else {
            access.requireMember(projectId, request.getAssigneeId());
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long projectId, Long taskId, Long userId) {
        access.requireEditor(projectId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        taskRepository.delete(task);
    }

    private Task.Status parseStatus(String s, Task.Status fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            return Task.Status.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + s);
        }
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getProject().getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus().name(),
                t.getAssignee() != null ? t.getAssignee().getId() : null,
                t.getAssignee() != null ? t.getAssignee().getDisplayName() : null,
                t.getDueDate(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}