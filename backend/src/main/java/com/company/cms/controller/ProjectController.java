package com.company.cms.controller;

import com.company.cms.dto.ProjectDtos.*;
import com.company.cms.entity.Employee;
import com.company.cms.repository.EmployeeRepository;
import com.company.cms.security.UserPrincipal;
import com.company.cms.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Endpoints for managing client/internal projects and staff assignments")
public class ProjectController {

    private final ProjectService projectService;
    private final EmployeeRepository employeeRepository;

    public ProjectController(ProjectService projectService, EmployeeRepository employeeRepository) {
        this.projectService = projectService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    @Operation(summary = "List projects (Employees see only assigned projects)")
    public ResponseEntity<List<ProjectResponse>> getProjects(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long employeeId = employeeRepository.findByUserId(userPrincipal.getId())
                .map(Employee::getId)
                .orElse(null);
        return ResponseEntity.ok(projectService.getProjectsForUser(userPrincipal.getRole(), employeeId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Admin/Manager: Create project")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Admin/Manager: Update project")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Admin/Manager: Assign employee to project")
    public ResponseEntity<ProjectResponse> assignEmployee(
            @PathVariable Long id,
            @Valid @RequestBody AssignEmployeeRequest request
    ) {
        return ResponseEntity.ok(projectService.assignEmployee(id, request.getEmployeeId()));
    }

    @DeleteMapping("/{id}/assignments/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Admin/Manager: Remove employee assignment from project")
    public ResponseEntity<ProjectResponse> removeEmployee(
            @PathVariable Long id,
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(projectService.removeEmployee(id, employeeId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete project")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }
}
