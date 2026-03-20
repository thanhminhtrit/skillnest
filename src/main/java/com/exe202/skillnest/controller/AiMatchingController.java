package com.exe202.skillnest.controller;

import com.exe202.skillnest.config.security.IsClient;
import com.exe202.skillnest.config.security.IsStudent;
import com.exe202.skillnest.dto.MatchingResultDTO;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.AiMatchingService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-matching")
@RequiredArgsConstructor
public class AiMatchingController {

    private final AiMatchingService aiMatchingService;
    private final SecurityUtil securityUtil;

    @PostMapping("/find-students")
    @IsClient
    @Operation(summary = "Find best matching students for a project (CLIENT only)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> findBestStudents(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = securityUtil.getCurrentUserId();
        List<MatchingResultDTO> results = aiMatchingService.findBestStudents(projectId, userId, limit);

        return ResponseEntity.ok(new BaseResponse(200,
                "Found " + results.size() + " matching students", results));
    }

    @PostMapping("/find-projects")
    @IsStudent
    @Operation(summary = "Find best matching projects for current student (STUDENT only)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> findBestProjects(
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = securityUtil.getCurrentUserId();
        List<MatchingResultDTO> results = aiMatchingService.findBestProjects(userId, limit);

        return ResponseEntity.ok(new BaseResponse(200,
                "Found " + results.size() + " matching projects", results));
    }
}
