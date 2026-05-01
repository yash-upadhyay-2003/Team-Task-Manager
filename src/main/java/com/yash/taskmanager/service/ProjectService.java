package com.yash.taskmanager.service;

import com.yash.taskmanager.dto.AddMemberDTO;
import com.yash.taskmanager.dto.ProjectRequestDTO;
import com.yash.taskmanager.dto.ProjectResponseDTO;
import com.yash.taskmanager.entity.Project;
import com.yash.taskmanager.entity.ProjectMember;
import com.yash.taskmanager.entity.Role;
import com.yash.taskmanager.entity.User;
import com.yash.taskmanager.exception.NotFoundException;
import com.yash.taskmanager.repository.ProjectMemberRepository;
import com.yash.taskmanager.repository.ProjectRepository;
import com.yash.taskmanager.repository.UserRepository;
import com.yash.taskmanager.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    // 🔥 CREATE PROJECT + ADD CREATOR AS ADMIN
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO request) {
        User currentUser = getCurrentUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        Project savedProject = projectRepository.save(project);

        ProjectMember creatorMember = new ProjectMember();
        creatorMember.setProject(savedProject);
        creatorMember.setUser(currentUser);
        creatorMember.setRole(Role.ADMIN); // ✅ FIX

        projectMemberRepository.save(creatorMember);

        return mapToDto(savedProject);
    }

    // 🔥 GET USER PROJECTS
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getUserProjects() {
        User currentUser = getCurrentUser();

        List<ProjectMember> memberships = projectMemberRepository.findByUser(currentUser);

        return memberships.stream()
                .map(ProjectMember::getProject)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 🔥 ADD MEMBER BY EMAIL (FINAL CLEAN VERSION)
    @Transactional
    public void addMember(Long projectId, AddMemberDTO request) {

        User currentUser = getCurrentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        // AUTH CHECK
        if (!project.getCreatedBy().getId().equals(currentUser.getId())
                && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Not authorized");
        }

        // FIND USER BY EMAIL
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // PREVENT ADDING CREATOR AGAIN
        if (project.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Creator already part of project");
        }

        // PREVENT DUPLICATE
        boolean exists = projectMemberRepository
                .findByProjectAndUser(project, user)
                .isPresent();

        if (exists) {
            throw new IllegalArgumentException("User already a member");
        }

        // CREATE MEMBER
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(Role.valueOf(request.getRole())); // 🔥 ROLE FIX

        projectMemberRepository.save(member);
    }

    // 🔥 GET MEMBERS (FOR UI)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMembers(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        return projectMemberRepository.findByProject(project)
        .stream()
        .map(m -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", m.getUser().getName());
            map.put("email", m.getUser().getEmail());
            map.put("role", m.getRole().name());
            return map;
        })
        .collect(java.util.stream.Collectors.toList());
    }

    // 🔥 REMOVE MEMBER (UNCHANGED BUT SAFE)
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        User currentUser = getCurrentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        boolean isCreator = project.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isCreator && !isAdmin) {
            throw new AccessDeniedException("Not authorized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (project.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot remove creator");
        }

        ProjectMember member = projectMemberRepository
                .findByProjectAndUser(project, user)
                .orElseThrow(() -> new IllegalArgumentException("Not a member"));

        projectMemberRepository.delete(member);
    }

    private ProjectResponseDTO mapToDto(Project project) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedBy().getId(),
                project.getCreatedBy().getName()
        );
    }

    private User getCurrentUser() {
        String email = SecurityUtils.getCurrentUserEmail();

        if (email == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}