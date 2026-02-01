package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.ProjectDTO;
import com.exe202.skillnest.payloads.request.CreateProjectRequest;
import com.exe202.skillnest.payloads.request.UpdateProjectRequest;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project APIs")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create new project (CLIENT only)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ProjectDTO projectDTO = projectService.createProject(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Project created successfully", projectDTO));
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Update project (owner only)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ProjectDTO projectDTO = projectService.updateProject(projectId, request, email);
        return ResponseEntity.ok(new BaseResponse(200, "Project updated successfully", projectDTO));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project (owner only)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> deleteProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        String email = authentication.getName();
        projectService.deleteProject(projectId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Project deleted successfully", null));
    }

    @PostMapping("/{projectId}/close")
    @Operation(summary = "Close project (owner only)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> closeProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        String email = authentication.getName();
        ProjectDTO projectDTO = projectService.closeProject(projectId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Project closed successfully", projectDTO));
    }

    @GetMapping("/public")
    @Operation(summary = "Get all open projects (public)")
    public ResponseEntity<BaseResponse> getAllOpenProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<ProjectDTO> projects = projectService.getAllOpenProjects(pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Projects retrieved successfully", projects));
    }

    @GetMapping("/public/{projectId}")
    @Operation(summary = "Get project by ID (public)")
    public ResponseEntity<BaseResponse> getProjectById(@PathVariable Long projectId) {
        ProjectDTO projectDTO = projectService.getProjectById(projectId);
        return ResponseEntity.ok(new BaseResponse(200, "Project retrieved successfully", projectDTO));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my projects", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectDTO> projects = projectService.getMyProjects(email, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Projects retrieved successfully", projects));
    }
}

