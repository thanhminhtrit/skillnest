package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.ProjectDTO;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.SavedProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saved-projects")
@RequiredArgsConstructor
@Tag(name = "Saved Projects", description = "APIs for managing saved/bookmarked projects")
@SecurityRequirement(name = "bearerAuth")
public class SavedProjectController {

    private final SavedProjectService savedProjectService;

    @PostMapping("/{projectId}")
    @Operation(summary = "Save a project", description = "Bookmark/save a project for later viewing")
    public ResponseEntity<BaseResponse> saveProject(
            @Parameter(description = "Project ID to save") @PathVariable Long projectId,
            Authentication authentication) {
        
        String email = authentication.getName();
        String message = savedProjectService.saveProject(projectId, email);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Project saved successfully", message));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Unsave a project", description = "Remove a project from saved/bookmarked projects")
    public ResponseEntity<BaseResponse> unsaveProject(
            @Parameter(description = "Project ID to unsave") @PathVariable Long projectId,
            Authentication authentication) {
        
        String email = authentication.getName();
        String message = savedProjectService.unsaveProject(projectId, email);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Project unsaved successfully", message));
    }

    @GetMapping
    @Operation(summary = "Get saved projects", description = "Get all projects saved/bookmarked by the current user")
    public ResponseEntity<BaseResponse> getSavedProjects(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectDTO> savedProjects = savedProjectService.getSavedProjects(email, pageable);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Saved projects retrieved successfully", savedProjects));
    }
}
