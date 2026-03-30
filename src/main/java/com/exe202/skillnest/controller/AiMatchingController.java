package com.exe202.skillnest.controller;

import com.exe202.skillnest.config.security.IsClient;
import com.exe202.skillnest.config.security.IsStudent;
import com.exe202.skillnest.dto.ProjectMatchingResponse;
import com.exe202.skillnest.dto.StudentMatchingResponse;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.AiMatchingService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-matching")
@RequiredArgsConstructor
public class AiMatchingController {

    private final AiMatchingService aiMatchingService;
    private final SecurityUtil securityUtil;

    @PostMapping("/find-students")
    @IsClient
    @Operation(summary = "Find best matching students for a project (CLIENT only)",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> findBestStudents(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = securityUtil.getCurrentUserId();
        StudentMatchingResponse response = aiMatchingService.findBestStudents(projectId, userId, page, limit);

        return ResponseEntity.ok(new BaseResponse(200,
                "Found " + response.getTotal() + " matching students", response));
    }

    @PostMapping("/find-projects")
    @IsStudent
    @Operation(summary = "Find best matching projects for current student (STUDENT only)",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> findBestProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = securityUtil.getCurrentUserId();
        ProjectMatchingResponse response = aiMatchingService.findBestProjects(userId, page, limit);

        return ResponseEntity.ok(new BaseResponse(200,
                "Found " + response.getTotal() + " matching projects", response));
    }

    @PostMapping("/invite")
    @IsClient
    @Operation(summary = "Invite a student to apply for project (CLIENT only)",
               description = "After using AI matching to find students, client can invite a specific student. " +
                       "Limited by subscription plan (FREE: 3, BASIC: 15, PRO: 50 per month).",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> inviteStudent(
            @Valid @RequestBody com.exe202.skillnest.dto.InviteStudentRequest request) {
        Long clientId = securityUtil.getCurrentUserId();
        aiMatchingService.inviteStudent(request.getProjectId(), request.getStudentId(), clientId);
        return ResponseEntity.ok(new BaseResponse(200, "Student invited successfully", null));
    }
}
