package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.*;

import java.util.List;

public interface ProfileService {
    ProfileResponseDTO getProfile(Long userId);
    ProfileResponseDTO updateProfile(Long userId, UpdateProfileRequest request);
    List<ApplicationHistoryDTO> getApplicationHistory(Long userId);
    ProfileStatsDTO getProfileStats(Long userId);
    MetadataResponseDTO getMetadata();

    // Email-based methods for controller
    ProfileResponseDTO getProfileByEmail(String email);
    ProfileResponseDTO updateProfileByEmail(String email, UpdateProfileRequest request);
    List<ApplicationHistoryDTO> getApplicationHistoryByEmail(String email);
    ProfileStatsDTO getProfileStatsByEmail(String email);

    // Avatar management methods
    ProfileResponseDTO updateAvatarByEmail(String email, String avatarUrl);
    ProfileResponseDTO deleteAvatarByEmail(String email);
}
