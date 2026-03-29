package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.*;
import com.exe202.skillnest.entity.AiMatchingHistory;
import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.entity.Skill;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.entity.UserProfile;
import com.exe202.skillnest.entity.UserSubscription;
import com.exe202.skillnest.enums.MatchingEntityType;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.repository.AiMatchingHistoryRepository;
import com.exe202.skillnest.repository.ProjectRepository;
import com.exe202.skillnest.repository.ProposalRepository;
import com.exe202.skillnest.repository.UserProfileRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.repository.UserSubscriptionRepository;
import com.exe202.skillnest.service.AiMatchingService;
import com.exe202.skillnest.service.OpenAIService;
import com.exe202.skillnest.service.ProfileService;
import com.exe202.skillnest.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AiMatchingServiceImpl implements AiMatchingService {

    private final OpenAIService openAIService;
    private final SubscriptionService subscriptionService;
    private final ProfileService profileService;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final ProposalRepository proposalRepo;
    private final AiMatchingHistoryRepository historyRepo;
    private final UserSubscriptionRepository subRepo;

    @Override
    public StudentMatchingResponse findBestStudents(Long projectId, Long userId, int page, int limit) {
        long start = System.currentTimeMillis();

        // 1. Check quota
        subscriptionService.checkAndIncrementAiMatchingUsage(userId);

        // 2. Get project with skills
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        Set<String> projectSkills = project.getSkills().stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());

        // 3. Query candidates from DB — filter by skills (or all active students if no skills)
        Set<Long> submittedStudentIds = new HashSet<>(proposalRepo.findStudentIdsByProjectId(projectId));
        List<User> candidates;
        if (!projectSkills.isEmpty()) {
            candidates = userRepo.findStudentsBySkills(new ArrayList<>(projectSkills));
        } else {
            candidates = userRepo.findAllActiveStudents();
        }
        candidates = candidates.stream()
                .filter(u -> !submittedStudentIds.contains(u.getUserId()))
                .toList();

        // 4. Score each candidate in Java — NO embedding API calls
        String projectText = buildProjectText(project);
        List<MatchingResultDTO> scored = candidates.stream()
                .map(student -> {
                    UserProfile profile = profileRepo.findByUser_UserId(student.getUserId()).orElse(null);
                    if (profile == null) return null;

                    Set<String> studentSkills = profile.getSkills() != null
                            ? profile.getSkills().stream().map(String::toLowerCase).collect(Collectors.toSet())
                            : Set.of();

                    long matchCount = studentSkills.stream().filter(projectSkills::contains).count();
                    double skillScore = projectSkills.isEmpty() ? 0.5
                            : (double) matchCount / projectSkills.size();

                    // GPA bonus (0–0.1)
                    double gpaBonus = (profile.getGpa() != null && profile.getGpa() >= 3.0)
                            ? (profile.getGpa() - 3.0) / 10.0 : 0;

                    double totalScore = Math.min(1.0, skillScore * 0.85 + gpaBonus + 0.05);

                    List<String> matchingSkills = studentSkills.stream()
                            .filter(projectSkills::contains)
                            .collect(Collectors.toList());

                    return MatchingResultDTO.builder()
                            .entityId(student.getUserId())
                            .entityType("STUDENT")
                            .name(student.getFullName())
                            .avatarUrl(student.getAvatarUrl())
                            .matchScore(totalScore)
                            .matchPercentage((int) (totalScore * 100))
                            .matchingSkills(matchingSkills)
                            .build();
                })
                .filter(Objects::nonNull)
                .filter(r -> r.getMatchScore() > 0.3)
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .toList();

        // 5. Generate AI explanation ONLY for top results with score >= 60% (max 5 API calls)
        List<MatchingResultDTO> results = scored.stream()
                .peek(r -> {
                    if (r.getMatchPercentage() >= 60) {
                        try {
                            String studentText = buildStudentText(userRepo.getReferenceById(r.getEntityId()));
                            r.setMatchReason(openAIService.generateMatchExplanation(
                                    projectText, studentText, r.getMatchScore()));
                        } catch (Exception e) {
                            r.setMatchReason("Good match based on " +
                                    r.getMatchingSkills().size() + " matching skills.");
                        }
                    } else {
                        r.setMatchReason("Partial match based on " +
                                r.getMatchingSkills().size() + " overlapping skills.");
                    }
                })
                .toList();

        // 6. Save history
        long execTime = System.currentTimeMillis() - start;
        saveHistory(userId, MatchingEntityType.PROJECT, projectId, results, execTime);

        // 7. Convert to StudentMatchDTO
        List<StudentMatchDTO> studentDTOs = results.stream()
                .map(r -> {
                    UserProfile profile = profileRepo.findByUser_UserId(r.getEntityId()).orElse(null);
                    return StudentMatchDTO.builder()
                            .studentId(r.getEntityId())
                            .name(r.getName())
                            .avatar(r.getAvatarUrl())
                            .university(profile != null ? profile.getUniversity() : null)
                            .major(profile != null ? profile.getMajor() : null)
                            .year(profile != null ? profile.getYear() : null)
                            .gpa(profile != null ? profile.getGpa() : null)
                            .skills(profile != null ? profile.getSkills() : List.of())
                            .matchingSkills(r.getMatchingSkills())
                            .matchScore(r.getMatchPercentage())
                            .matchReason(r.getMatchReason())
                            .build();
                })
                .toList();

        // 8. Apply pagination
        int fromIndex = Math.min(page * limit, studentDTOs.size());
        int toIndex = Math.min(fromIndex + limit, studentDTOs.size());

        return StudentMatchingResponse.builder()
                .students(studentDTOs.subList(fromIndex, toIndex))
                .total(studentDTOs.size())
                .page(page)
                .limit(limit)
                .build();
    }

    @Override
    public ProjectMatchingResponse findBestProjects(Long userId, int page, int limit) {
        long start = System.currentTimeMillis();

        // 1. Check quota
        subscriptionService.checkAndIncrementAiMatchingUsage(userId);

        // 2. Get student + profile skills
        User student = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserProfile studentProfile = profileRepo.findByUser_UserId(userId).orElse(null);
        Set<String> studentSkills = (studentProfile != null && studentProfile.getSkills() != null)
                ? studentProfile.getSkills().stream().map(String::toLowerCase).collect(Collectors.toSet())
                : Set.of();

        // 3. DB-filter open projects by skill overlap (or all open if no skills)
        List<Project> projects;
        if (!studentSkills.isEmpty()) {
            projects = projectRepo.findOpenProjectsBySkills(new ArrayList<>(studentSkills));
        } else {
            projects = projectRepo.findAllOpenProjects();
        }

        // 4. Score each project in Java — NO embedding API calls
        String studentText = buildStudentText(student);
        Map<Long, Project> projectMap = projects.stream()
                .collect(Collectors.toMap(Project::getProjectId, p -> p));

        List<MatchingResultDTO> rawResults = projects.stream()
                .map(project -> {
                    Set<String> projectSkillNames = project.getSkills().stream()
                            .map(skill -> skill.getName().toLowerCase())
                            .collect(Collectors.toSet());

                    long matchCount = studentSkills.stream().filter(projectSkillNames::contains).count();
                    double skillScore = projectSkillNames.isEmpty() ? 0.5
                            : (double) matchCount / projectSkillNames.size();

                    double totalScore = Math.min(1.0, skillScore * 0.9 + 0.05);

                    List<String> matchingSkills = studentSkills.stream()
                            .filter(projectSkillNames::contains)
                            .collect(Collectors.toList());

                    return MatchingResultDTO.builder()
                            .entityId(project.getProjectId())
                            .entityType("PROJECT")
                            .name(project.getTitle())
                            .matchScore(totalScore)
                            .matchPercentage((int) (totalScore * 100))
                            .matchingSkills(matchingSkills)
                            .build();
                })
                .filter(r -> r.getMatchScore() > 0.3)
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .toList();

        // 5. Generate AI explanation ONLY for top results with score >= 60% (max 5 API calls)
        rawResults = rawResults.stream()
                .peek(r -> {
                    if (r.getMatchPercentage() >= 60) {
                        try {
                            Project p = projectMap.get(r.getEntityId());
                            String projectText = buildProjectText(p);
                            r.setMatchReason(openAIService.generateMatchExplanation(
                                    projectText, studentText, r.getMatchScore()));
                        } catch (Exception e) {
                            r.setMatchReason("Good match based on " +
                                    r.getMatchingSkills().size() + " matching skills.");
                        }
                    } else {
                        r.setMatchReason("Partial match based on " +
                                r.getMatchingSkills().size() + " overlapping skills.");
                    }
                })
                .toList();

        // 6. Save history
        long execTime = System.currentTimeMillis() - start;
        saveHistory(userId, MatchingEntityType.STUDENT, null, rawResults, execTime);

        // 7. Get student profile DTO for response
        ProfileResponseDTO studentProfileDTO = profileService.getProfile(userId);

        // 8. Convert to ProjectMatchDTO
        List<ProjectMatchDTO> projectDTOs = rawResults.stream()
                .map(r -> {
                    Project p = projectMap.get(r.getEntityId());
                    return ProjectMatchDTO.builder()
                            .projectId(r.getEntityId())
                            .title(r.getName())
                            .companyName(p != null ? p.getClient().getFullName() : null)
                            .location(p != null ? p.getLocation() : null)
                            .employmentType(p != null ? p.getEmploymentType() : null)
                            .budgetMin(p != null ? p.getBudgetMin() : null)
                            .budgetMax(p != null ? p.getBudgetMax() : null)
                            .currency(p != null ? p.getCurrency() : null)
                            .postedAgo(p != null ? formatPostedAgo(p.getCreatedAt()) : null)
                            .matchScore(r.getMatchPercentage())
                            .matchReason(r.getMatchReason())
                            .build();
                })
                .toList();

        // 9. Calculate stats
        int totalMatches = projectDTOs.size();
        int highMatches = (int) projectDTOs.stream().filter(p -> p.getMatchScore() >= 80).count();
        int averageScore = totalMatches > 0
                ? (int) projectDTOs.stream().mapToInt(ProjectMatchDTO::getMatchScore).average().orElse(0)
                : 0;

        MatchingStatsDTO stats = MatchingStatsDTO.builder()
                .totalMatches(totalMatches)
                .highMatches(highMatches)
                .averageScore(averageScore)
                .build();

        // 10. Apply pagination
        int fromIndex = Math.min(page * limit, projectDTOs.size());
        int toIndex = Math.min(fromIndex + limit, projectDTOs.size());
        List<ProjectMatchDTO> pageContent = projectDTOs.subList(fromIndex, toIndex);

        return ProjectMatchingResponse.builder()
                .studentProfile(studentProfileDTO)
                .stats(stats)
                .projects(pageContent)
                .total(totalMatches)
                .page(page)
                .limit(limit)
                .build();
    }

    private String buildProjectText(Project p) {
        String skills = p.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
        return String.format("Title: %s\nDesc: %s\nSkills: %s\nLocation: %s",
                p.getTitle(), p.getDescription(), skills, p.getLocation());
    }

    private String buildStudentText(User s) {
        UserProfile p = profileRepo.findByUser_UserId(s.getUserId()).orElse(null);
        if (p == null) return s.getFullName();
        return String.format("Name: %s\nUniv: %s\nSkills: %s\nBio: %s",
                s.getFullName(), p.getUniversity(),
                String.join(", ", p.getSkills()), p.getBio());
    }

    private String formatPostedAgo(LocalDateTime createdAt) {
        if (createdAt == null) return null;
        long days = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        if (days == 0) return "Today";
        if (days == 1) return "1 day ago";
        if (days < 7) return days + " days ago";
        if (days < 30) return (days / 7) + " week(s) ago";
        return (days / 30) + " month(s) ago";
    }

    private void saveHistory(Long userId, MatchingEntityType entityType, Long entityId,
                             List<MatchingResultDTO> results, long execTimeMs) {
        UserSubscription subscription = subRepo.findActiveByUserId(userId).orElse(null);

        Map<String, Object> resultsMap = new HashMap<>();
        resultsMap.put("matches", results.stream()
                .map(r -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("entityId", r.getEntityId());
                    entry.put("name", r.getName());
                    entry.put("matchScore", r.getMatchScore());
                    entry.put("matchPercentage", r.getMatchPercentage());
                    return entry;
                })
                .toList());

        AiMatchingHistory history = AiMatchingHistory.builder()
                .user(userRepo.getReferenceById(userId))
                .subscription(subscription)
                .entityType(entityType)
                .entityId(entityId)
                .results(resultsMap)
                .matchCount(results.size())
                .executionTimeMs((int) execTimeMs)
                .build();

        historyRepo.save(history);
    }
}
