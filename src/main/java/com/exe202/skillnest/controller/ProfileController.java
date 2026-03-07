package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.*;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile", description = "Profile management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get current user profile", description = "Returns the profile information of the authenticated user")
    public ResponseEntity<BaseResponse> getProfile(Authentication authentication) {
        log.info("GET /api/profile - Getting current user profile");

        String email = authentication.getName();
        ProfileResponseDTO profile = profileService.getProfileByEmail(email);

        return ResponseEntity.ok(new BaseResponse(200, "Profile retrieved successfully", profile));
    }

    @PutMapping
    @Operation(summary = "Update profile", description = "Update the profile information of the authenticated user")
    public ResponseEntity<BaseResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        log.info("PUT /api/profile - Updating profile");

        String email = authentication.getName();
        ProfileResponseDTO updatedProfile = profileService.updateProfileByEmail(email, request);

        return ResponseEntity.ok(new BaseResponse(200, "Profile updated successfully", updatedProfile));
    }

    @GetMapping("/applications")
    @Operation(summary = "Get application history", description = "Returns the job application history of the authenticated user")
    public ResponseEntity<BaseResponse> getApplicationHistory(Authentication authentication) {
        log.info("GET /api/profile/applications - Getting application history");

        String email = authentication.getName();
        List<ApplicationHistoryDTO> applications = profileService.getApplicationHistoryByEmail(email);

        return ResponseEntity.ok(new BaseResponse(200, "Application history retrieved successfully", applications));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get profile statistics", description = "Returns statistics including application counts and profile completion percentage")
    public ResponseEntity<BaseResponse> getProfileStats(Authentication authentication) {
        log.info("GET /api/profile/stats - Getting profile stats");

        String email = authentication.getName();
        ProfileStatsDTO stats = profileService.getProfileStatsByEmail(email);

        return ResponseEntity.ok(new BaseResponse(200, "Profile stats retrieved successfully", stats));
    }

    @GetMapping("/metadata")
    @Operation(summary = "Get metadata options", description = "Returns available options for locations and job types")
    public ResponseEntity<BaseResponse> getMetadata() {
        log.info("GET /api/profile/metadata - Getting metadata");

        MetadataResponseDTO metadata = profileService.getMetadata();

        return ResponseEntity.ok(new BaseResponse(200, "Metadata retrieved successfully", metadata));
    }
}
