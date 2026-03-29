package com.exe202.skillnest.controller;

import com.exe202.skillnest.config.security.IsAdmin;
import com.exe202.skillnest.config.security.IsManager;
import com.exe202.skillnest.dto.UserDTO;
import com.exe202.skillnest.enums.UserStatus;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.AdminService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "User management and system administration")
@SecurityRequirement(name = "bearer-jwt")
public class AdminController {

    private final AdminService adminService;
    private final SecurityUtil securityUtil;

    @GetMapping("/users")
    @IsManager
    @Operation(summary = "Get all users with filters (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserDTO> users = adminService.getAllUsers(role, status, search, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Users retrieved successfully", users));
    }

    @GetMapping("/users/{userId}")
    @IsManager
    @Operation(summary = "Get user details (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> getUserDetails(@PathVariable Long userId) {
        UserDTO user = adminService.getUserDetails(userId);
        return ResponseEntity.ok(new BaseResponse(200, "User details retrieved successfully", user));
    }

    @PutMapping("/users/{userId}")
    @IsManager
    @Operation(summary = "Update user profile (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> updates) {
        UserDTO user = adminService.updateUserProfile(userId, updates);
        return ResponseEntity.ok(new BaseResponse(200, "User profile updated successfully", user));
    }

    @PutMapping("/users/{userId}/status")
    @IsManager
    @Operation(summary = "Change user status (ADMIN: all statuses, MANAGER: only ACTIVE/SUSPENDED)")
    public ResponseEntity<BaseResponse> changeUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status) {
        Long adminId = securityUtil.getCurrentUserId();
        UserDTO user = adminService.changeUserStatus(userId, status, adminId);
        return ResponseEntity.ok(new BaseResponse(200, "User status changed successfully", user));
    }

    @DeleteMapping("/users/{userId}")
    @IsAdmin
    @Operation(summary = "Delete user permanently (ADMIN only)")
    public ResponseEntity<BaseResponse> deleteUser(@PathVariable Long userId) {
        Long adminId = securityUtil.getCurrentUserId();
        adminService.deleteUser(userId, adminId);
        return ResponseEntity.ok(new BaseResponse(200, "User deleted successfully", null));
    }
}
