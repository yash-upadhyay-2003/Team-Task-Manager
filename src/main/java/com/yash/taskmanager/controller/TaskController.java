package com.yash.taskmanager.controller;

import com.yash.taskmanager.dto.ApiResponse;
import com.yash.taskmanager.dto.TaskRequestDTO;
import com.yash.taskmanager.dto.TaskResponseDTO;
import com.yash.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDTO>> createTask(@Valid @RequestBody TaskRequestDTO request) {
        TaskResponseDTO response = taskService.createTask(request);
        return ResponseEntity.ok(new ApiResponse<>("Task created successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getMyTasks() {
        List<TaskResponseDTO> tasks = taskService.getMyTasks();
        return ResponseEntity.ok(new ApiResponse<>("Tasks retrieved successfully", tasks));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getProjectTasks(@PathVariable Long projectId) {
        List<TaskResponseDTO> tasks = taskService.getTasksForProject(projectId);
        return ResponseEntity.ok(new ApiResponse<>("Project tasks retrieved successfully", tasks));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");

        if (status == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
        }

        taskService.updateStatus(taskId, status);
        return ResponseEntity.ok(Map.of("message", "Task updated successfully"));
    }
}
