package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.*;
import com.exe202.skillnest.entity.JobApplication;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.entity.UserProfile;
import com.exe202.skillnest.enums.ApplicationStatus;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.repository.JobApplicationRepository;
import com.exe202.skillnest.repository.UserProfileRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfile(Long userId) {
        log.info("Getting profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElse(createDefaultProfile(user));

        return ProfileResponseDTO.builder()
                .userId(user.getUserId())
                .name(user.getFullName())
                .email(user.getEmail())
                .avatar(user.getAvatarUrl())
                .phone(user.getPhone())
                .address(profile.getAddress())
                .university(profile.getUniversity())
                .major(profile.getMajor())
                .year(profile.getYear())
                .gpa(profile.getGpa())
                .bio(profile.getBio())
                .skills(profile.getSkills() != null ? profile.getSkills() : new ArrayList<>())
                .interests(profile.getInterests() != null ? profile.getInterests() : new ArrayList<>())
                .preferredLocations(profile.getPreferredLocations() != null ? profile.getPreferredLocations() : new ArrayList<>())
                .preferredJobTypes(profile.getPreferredJobTypes() != null ? profile.getPreferredJobTypes() : new ArrayList<>())
                .build();
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Update user basic info
        user.setFullName(request.getName());
        user.setPhone(request.getPhone());
        if (request.getAvatar() != null) {
            user.setAvatarUrl(request.getAvatar());
        }
        userRepository.save(user);

        // Update or create profile
        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElse(UserProfile.builder()
                        .user(user)
                        .build());

        profile.setAddress(request.getAddress());
        profile.setUniversity(request.getUniversity());
        profile.setMajor(request.getMajor());
        profile.setYear(request.getYear());
        profile.setGpa(request.getGpa());
        profile.setBio(request.getBio());
        profile.setSkills(request.getSkills() != null ? request.getSkills() : new ArrayList<>());
        profile.setInterests(request.getInterests() != null ? request.getInterests() : new ArrayList<>());
        profile.setPreferredLocations(request.getPreferredLocations() != null ? request.getPreferredLocations() : new ArrayList<>());
        profile.setPreferredJobTypes(request.getPreferredJobTypes() != null ? request.getPreferredJobTypes() : new ArrayList<>());

        userProfileRepository.save(profile);

        log.info("Profile updated successfully for user: {}", userId);
        return getProfile(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationHistoryDTO> getApplicationHistory(Long userId) {
        log.info("Getting application history for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }

        List<JobApplication> applications = jobApplicationRepository
                .findByUser_UserIdOrderByAppliedDateDesc(userId);

        return applications.stream()
                .map(app -> ApplicationHistoryDTO.builder()
                        .id(app.getApplicationId())
                        .jobId(app.getJobId())
                        .jobTitle(app.getJobTitle())
                        .companyName(app.getCompanyName())
                        .status(app.getStatus())
                        .appliedDate(app.getAppliedDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileStatsDTO getProfileStats(Long userId) {
        log.info("Getting profile stats for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElse(createDefaultProfile(user));

        // Calculate total applications
        long totalApplications = jobApplicationRepository.countByUser_UserId(userId);

        // Get applications by status
        List<JobApplication> pendingApps = jobApplicationRepository
                .findByUser_UserIdAndStatus(userId, ApplicationStatus.PENDING);
        List<JobApplication> interviewApps = jobApplicationRepository
                .findByUser_UserIdAndStatus(userId, ApplicationStatus.INTERVIEW);
        List<JobApplication> rejectedApps = jobApplicationRepository
                .findByUser_UserIdAndStatus(userId, ApplicationStatus.REJECTED);

        // Calculate profile completion
        int completion = calculateProfileCompletion(user, profile);

        return ProfileStatsDTO.builder()
                .totalApplications((int) totalApplications)
                .profileCompletion(completion)
                .pending(ProfileStatsDTO.StatusDetail.builder()
                        .count(pendingApps.size())
                        .jobIds(pendingApps.stream().map(JobApplication::getJobId).collect(Collectors.toList()))
                        .build())
                .interviewed(ProfileStatsDTO.StatusDetail.builder()
                        .count(interviewApps.size())
                        .jobIds(interviewApps.stream().map(JobApplication::getJobId).collect(Collectors.toList()))
                        .build())
                .rejected(ProfileStatsDTO.StatusDetail.builder()
                        .count(rejectedApps.size())
                        .jobIds(rejectedApps.stream().map(JobApplication::getJobId).collect(Collectors.toList()))
                        .build())
                .build();
    }

    @Override
    public MetadataResponseDTO getMetadata() {
        log.info("Getting metadata for profile options");

        List<String> locations = Arrays.asList(
                "Hà Nội",
                "Hồ Chí Minh",
                "Đà Nẵng",
                "Hải Phòng",
                "Cần Thơ",
                "Remote"
        );

        List<String> jobTypes = Arrays.asList(
                "Full-time",
                "Part-time",
                "Thực tập",
                "Freelance",
                "Contract"
        );

        return MetadataResponseDTO.builder()
                .locations(locations)
                .jobTypes(jobTypes)
                .build();
    }

    // Email-based methods implementation
    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfileByEmail(String email) {
        log.info("Getting profile for user email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return getProfile(user.getUserId());
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateProfileByEmail(String email, UpdateProfileRequest request) {
        log.info("Updating profile for user email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return updateProfile(user.getUserId(), request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationHistoryDTO> getApplicationHistoryByEmail(String email) {
        log.info("Getting application history for user email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return getApplicationHistory(user.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileStatsDTO getProfileStatsByEmail(String email) {
        log.info("Getting profile stats for user email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return getProfileStats(user.getUserId());
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateAvatarByEmail(String email, String avatarUrl) {
        log.info("Updating avatar for user email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // Update avatar URL
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        log.info("Avatar updated successfully for user: {}", email);
        return getProfile(user.getUserId());
    }

    @Override
    @Transactional
    public ProfileResponseDTO deleteAvatarByEmail(String email) {
        log.info("Deleting avatar for user email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // Remove avatar URL
        user.setAvatarUrl(null);
        userRepository.save(user);

        log.info("Avatar deleted successfully for user: {}", email);
        return getProfile(user.getUserId());
    }

    /**
     * Calculate profile completion percentage (0-100)
     * Based on:
     * - Name: +5
     * - Avatar: +10
     * - Phone: +10
     * - Address: +10
     * - Bio: +10
     * - GPA: +10
     * - Skills (>=3): +20
     * - Interests (>=1): +10
     * - Preferred locations (>=1): +10
     * - Preferred job types (>=1): +5
     * Total: 100
     */
    private int calculateProfileCompletion(User user, UserProfile profile) {
        int score = 0;

        // Name (5 points)
        if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            score += 5;
        }

        // Avatar (10 points)
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
            score += 10;
        }

        // Phone (10 points)
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            score += 10;
        }

        // Address (10 points)
        if (profile.getAddress() != null && !profile.getAddress().trim().isEmpty()) {
            score += 10;
        }

        // Bio (10 points)
        if (profile.getBio() != null && !profile.getBio().trim().isEmpty()) {
            score += 10;
        }

        // GPA (10 points)
        if (profile.getGpa() != null && profile.getGpa() > 0) {
            score += 10;
        }

        // Skills - at least 3 (20 points)
        if (profile.getSkills() != null && profile.getSkills().size() >= 3) {
            score += 20;
        }

        // Interests - at least 1 (10 points)
        if (profile.getInterests() != null && !profile.getInterests().isEmpty()) {
            score += 10;
        }

        // Preferred locations - at least 1 (10 points)
        if (profile.getPreferredLocations() != null && !profile.getPreferredLocations().isEmpty()) {
            score += 10;
        }

        // Preferred job types - at least 1 (5 points)
        if (profile.getPreferredJobTypes() != null && !profile.getPreferredJobTypes().isEmpty()) {
            score += 5;
        }

        return Math.min(100, Math.max(0, score));
    }

    private UserProfile createDefaultProfile(User user) {
        return UserProfile.builder()
                .user(user)
                .skills(new ArrayList<>())
                .interests(new ArrayList<>())
                .preferredLocations(new ArrayList<>())
                .preferredJobTypes(new ArrayList<>())
                .build();
    }
}
