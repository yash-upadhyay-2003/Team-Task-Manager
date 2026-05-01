package com.yash.taskmanager.service;

import com.yash.taskmanager.dto.TaskRequestDTO;
import com.yash.taskmanager.dto.TaskResponseDTO;
import com.yash.taskmanager.dto.TaskStatusUpdateDTO;
import com.yash.taskmanager.entity.Project;
import com.yash.taskmanager.entity.Task;
import com.yash.taskmanager.entity.TaskStatus;
import com.yash.taskmanager.entity.User;
import com.yash.taskmanager.exception.NotFoundException;
import com.yash.taskmanager.repository.ProjectMemberRepository;
import com.yash.taskmanager.repository.ProjectRepository;
import com.yash.taskmanager.repository.TaskRepository;
import com.yash.taskmanager.repository.UserRepository;
import com.yash.taskmanager.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (!hasProjectAccess(project, currentUser)) {
            throw new AccessDeniedException("Not authorized to create tasks for this project");
        }

        User assignee = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new NotFoundException("Assigned user not found"));

        if (!isProjectMemberOrOwner(project, assignee)) {
            throw new IllegalArgumentException("Assigned user must be a member of the project");
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .dueDate(request.getDueDate())
                .project(project)
                .assignedTo(assignee)
                .createdBy(currentUser)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToDto(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksForProject(Long projectId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (!hasProjectAccess(project, currentUser)) {
            throw new AccessDeniedException("Not authorized to view tasks for this project");
        }

        return taskRepository.findByProject(project).stream()
                .filter(task ->
                        task.getAssignedTo().getId().equals(currentUser.getId())
                                || task.getCreatedBy().getId().equals(currentUser.getId())
                )
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getMyTasks() {
        User currentUser = getCurrentUser();
        return taskRepository.findByAssignedTo(currentUser).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.yash.taskmanager.dto.DashboardResponseDTO getDashboard() {
        User currentUser = getCurrentUser();
        long totalTasks = taskRepository.countByAssignedTo(currentUser);
        long completedTasks = taskRepository.countByAssignedToAndStatus(currentUser, TaskStatus.DONE);
        long pendingTasks = taskRepository.countByAssignedToAndStatusNot(currentUser, TaskStatus.DONE);
        long overdueTasks = taskRepository.countByAssignedToAndDueDateBeforeAndStatusNot(currentUser, LocalDate.now(), TaskStatus.DONE);

        return new com.yash.taskmanager.dto.DashboardResponseDTO(totalTasks, completedTasks, pendingTasks, overdueTasks);
    }

    @Transactional
    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatusUpdateDTO request) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!canModifyTask(task, currentUser)) {
            throw new AccessDeniedException("Not authorized to update this task");
        }

        task.setStatus(request.getStatus());
        Task updated = taskRepository.save(task);
        return mapToDto(updated);
    }

    @Transactional
    public void updateStatus(Long taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        try {
            task.setStatus(TaskStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status, e);
        }

        taskRepository.save(task);
    }

    private boolean canModifyTask(Task task, User user) {
        return task.getAssignedTo().equals(user)
                || task.getCreatedBy().equals(user)
                || user.getRole() == com.yash.taskmanager.entity.Role.ADMIN;
    }

    private boolean hasProjectAccess(Project project, User user) {
    boolean isCreator = project.getCreatedBy() != null
            && project.getCreatedBy().getId() != null
            && project.getCreatedBy().getId().equals(user.getId());

    return isCreator || projectMemberRepository
            .findByProjectAndUser(project, user)
            .isPresent();
}

private boolean isProjectMemberOrOwner(Project project, User user) {
    boolean isCreator = project.getCreatedBy() != null
            && project.getCreatedBy().getId() != null
            && project.getCreatedBy().getId().equals(user.getId());

    return isCreator || projectMemberRepository
            .findByProjectAndUser(project, user)
            .isPresent();
}

    private TaskResponseDTO mapToDto(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getAssignedTo().getId(),
                task.getAssignedTo().getName(),
                task.getCreatedBy().getId(),
                task.getCreatedBy().getName()
        );
    }

    private User getCurrentUser() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new UsernameNotFoundException("Authenticated user not found");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }
}
