package com.yash.taskmanager.controller;

import com.yash.taskmanager.dto.AddMemberDTO;
import com.yash.taskmanager.dto.ApiResponse;
import com.yash.taskmanager.dto.ProjectRequestDTO;
import com.yash.taskmanager.dto.ProjectResponseDTO;
import com.yash.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<ProjectResponseDTO>> createProject(@Valid @RequestBody ProjectRequestDTO request) {
        ProjectResponseDTO response = projectService.createProject(request);
        return ResponseEntity.ok(new ApiResponse<>("Project created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponseDTO>>> getProjects() {
        List<ProjectResponseDTO> projects = projectService.getUserProjects();
        return ResponseEntity.ok(new ApiResponse<>("Projects retrieved successfully", projects));
    }

    // ✅ FIXED HERE
    @PostMapping("/{projectId}/add-member")
    public ResponseEntity<?> addMember(
            @PathVariable Long projectId,
            @RequestBody AddMemberDTO request
    ) {
        projectService.addMember(projectId, request); // ✅ PASS WHOLE DTO
        return ResponseEntity.ok(Map.of("message", "Member added successfully"));
    }

    @DeleteMapping("/{projectId}/remove-member/{userId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {

        projectService.removeMember(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }

    // ✅ OPTIONAL (FOR FRONTEND MEMBERS LIST)
    @GetMapping("/{projectId}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getMembers(projectId));
    }
}