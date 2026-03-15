package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.UserDTO;
import com.exe202.skillnest.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AdminService {

    /**
     * Get all users with filters
     */
    Page<UserDTO> getAllUsers(String role, UserStatus status, String search, Pageable pageable);

    /**
     * Update user profile (Admin/Manager)
     */
    UserDTO updateUserProfile(Long userId, Map<String, Object> updates);

    /**
     * Change user status (Admin: all statuses, Manager: only ACTIVE/SUSPENDED)
     */
    UserDTO changeUserStatus(Long userId, UserStatus newStatus, Long adminId);

    /**
     * Delete user permanently (Admin only)
     */
    void deleteUser(Long userId, Long adminId);

    /**
     * Get user details
     */
    UserDTO getUserDetails(Long userId);
}

