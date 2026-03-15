package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.UserDTO;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.entity.UserProfile;
import com.exe202.skillnest.enums.UserStatus;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.mapper.UserMapper;
import com.exe202.skillnest.repository.UserProfileRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(String role, UserStatus status, String search, Pageable pageable) {
        // Implement filtering logic based on parameters
        if (search != null && !search.isEmpty()) {
            return userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(search, search, pageable)
                    .map(userMapper::toDTO);
        }

        if (status != null) {
            return userRepository.findByStatus(status, pageable)
                    .map(userMapper::toDTO);
        }

        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    @Override
    @Transactional
    public UserDTO updateUserProfile(Long userId, Map<String, Object> updates) {
        log.info("Updating user profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElse(UserProfile.builder().user(user).build());

        // Update user fields
        if (updates.containsKey("fullName")) {
            user.setFullName((String) updates.get("fullName"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) updates.get("avatarUrl"));
        }

        // Update profile fields
        if (updates.containsKey("bio")) {
            profile.setBio((String) updates.get("bio"));
        }
        if (updates.containsKey("university")) {
            profile.setUniversity((String) updates.get("university"));
        }
        if (updates.containsKey("major")) {
            profile.setMajor((String) updates.get("major"));
        }
        if (updates.containsKey("address")) {
            profile.setAddress((String) updates.get("address"));
        }
        // Note: companyName and taxCode belong to CompanyInfo entity, not UserProfile

        userRepository.save(user);
        userProfileRepository.save(profile);

        log.info("User profile updated successfully for userId: {}", userId);
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO changeUserStatus(Long userId, UserStatus newStatus, Long adminId) {
        log.info("Admin {} changing status of user {} to {}", adminId, userId, newStatus);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        // Check permissions
        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        boolean isManager = admin.getRoles().stream()
                .anyMatch(role -> role.getName().equals("MANAGER"));

        if (!isAdmin && !isManager) {
            throw new ForbiddenException("Only Admin or Manager can change user status");
        }

        // Manager can only toggle between ACTIVE and SUSPENDED
        if (isManager && !isAdmin) {
            if (newStatus != UserStatus.ACTIVE && newStatus != UserStatus.SUSPENDED) {
                throw new ForbiddenException("Manager can only change status to ACTIVE or SUSPENDED");
            }
            if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.SUSPENDED) {
                throw new ForbiddenException("Manager cannot modify users with status: " + user.getStatus());
            }
        }

        user.setStatus(newStatus);
        userRepository.save(user);

        log.info("User status changed successfully for userId: {}", userId);
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, Long adminId) {
        log.info("Admin {} deleting user {}", adminId, userId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        if (!isAdmin) {
            throw new ForbiddenException("Only Admin can delete users permanently");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Soft delete by setting status to DELETED
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User deleted successfully: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDTO(user);
    }
}
