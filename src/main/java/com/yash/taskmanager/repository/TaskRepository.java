package com.yash.taskmanager.repository;

import com.yash.taskmanager.entity.Project;
import com.yash.taskmanager.entity.Task;
import com.yash.taskmanager.entity.TaskStatus;
import com.yash.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findByAssignedTo(User assignedTo);

    long countByAssignedTo(User assignedTo);
    long countByAssignedToAndStatus(User assignedTo, TaskStatus status);
    long countByAssignedToAndStatusNot(User assignedTo, TaskStatus status);
    long countByAssignedToAndDueDateBeforeAndStatusNot(User assignedTo, LocalDate dueDate, TaskStatus status);
}

