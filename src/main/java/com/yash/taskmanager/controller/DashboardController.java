package com.yash.taskmanager.controller;

import com.yash.taskmanager.dto.ApiResponse;
import com.yash.taskmanager.dto.DashboardResponseDTO;
import com.yash.taskmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final TaskService taskService;

    public DashboardController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponseDTO>> getDashboard() {
        DashboardResponseDTO dashboard = taskService.getDashboard();
        return ResponseEntity.ok(new ApiResponse<>("Dashboard data fetched", dashboard));
    }
}
