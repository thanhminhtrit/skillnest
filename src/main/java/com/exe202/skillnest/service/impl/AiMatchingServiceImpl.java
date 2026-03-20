package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.MatchingResultDTO;
import com.exe202.skillnest.entity.AiMatchingHistory;
import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.entity.Skill;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.entity.UserProfile;
import com.exe202.skillnest.entity.UserSubscription;
import com.exe202.skillnest.enums.MatchingEntityType;
import com.exe202.skillnest.enums.ProjectStatus;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.repository.AiMatchingHistoryRepository;
import com.exe202.skillnest.repository.ProjectRepository;
import com.exe202.skillnest.repository.UserProfileRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.repository.UserSubscriptionRepository;
import com.exe202.skillnest.service.AiMatchingService;
import com.exe202.skillnest.service.OpenAIService;
import com.exe202.skillnest.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AiMatchingServiceImpl implements AiMatchingService {

    private final OpenAIService openAIService;
    private final SubscriptionService subscriptionService;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final AiMatchingHistoryRepository historyRepo;
    private final UserSubscriptionRepository subRepo;

    @Override
    public List<MatchingResultDTO> findBestStudents(Long projectId, Long userId, int limit) {
        long start = System.currentTimeMillis();

        // 1. Check quota
        subscriptionService.checkAndIncrementAiMatchingUsage(userId);

        // 2. Get project
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        // 3. Pre-filter students (rule-based)
        List<User> candidates = userRepo.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("STUDENT")))
                .limit(50)
                .toList();

        // 4. Generate project embedding
        String projectText = buildProjectText(project);
        double[] projectEmb = openAIService.generateEmbedding(projectText);

        // 5. Score each candidate
        List<MatchingResultDTO> results = candidates.stream()
                .map(student -> {
                    String studentText = buildStudentText(student);
                    double[] studentEmb = openAIService.generateEmbedding(studentText);
                    double score = openAIService.cosineSimilarity(projectEmb, studentEmb);

                    // Boost by GPA
                    UserProfile profile = profileRepo.findByUser_UserId(student.getUserId()).orElse(null);
                    if (profile != null && profile.getGpa() != null && profile.getGpa() >= 3.5) {
                        score += 0.05;
                    }

                    String reason = openAIService.generateMatchExplanation(projectText, studentText, score);

                    return MatchingResultDTO.builder()
                            .entityId(student.getUserId())
                            .entityType("STUDENT")
                            .name(student.getFullName())
                            .avatarUrl(student.getAvatarUrl())
                            .matchScore(score)
                            .matchPercentage((int) (score * 100))
                            .matchReason(reason)
                            .build();
                })
                .filter(r -> r.getMatchScore() > 0.5)
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .limit(limit)
                .toList();

        // 6. Save history
        long execTime = System.currentTimeMillis() - start;
        saveHistory(userId, MatchingEntityType.PROJECT, projectId, results, execTime);

        return results;
    }

    @Override
    public List<MatchingResultDTO> findBestProjects(Long userId, int limit) {
        long start = System.currentTimeMillis();

        // 1. Check quota
        subscriptionService.checkAndIncrementAiMatchingUsage(userId);

        // 2. Get student
        User student = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 3. Build student embedding
        String studentText = buildStudentText(student);
        double[] studentEmb = openAIService.generateEmbedding(studentText);

        // 4. Pre-filter open projects
        List<Project> projects = projectRepo
                .findByStatus(ProjectStatus.OPEN, PageRequest.of(0, 50))
                .getContent();

        // 5. Score each project
        List<MatchingResultDTO> results = projects.stream()
                .map(project -> {
                    String projectText = buildProjectText(project);
                    double[] projectEmb = openAIService.generateEmbedding(projectText);
                    double score = openAIService.cosineSimilarity(studentEmb, projectEmb);
                    String reason = openAIService.generateMatchExplanation(projectText, studentText, score);

                    return MatchingResultDTO.builder()
                            .entityId(project.getProjectId())
                            .entityType("PROJECT")
                            .name(project.getTitle())
                            .avatarUrl(null)
                            .matchScore(score)
                            .matchPercentage((int) (score * 100))
                            .matchReason(reason)
                            .build();
                })
                .filter(r -> r.getMatchScore() > 0.5)
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .limit(limit)
                .toList();

        // 6. Save history
        long execTime = System.currentTimeMillis() - start;
        saveHistory(userId, MatchingEntityType.STUDENT, null, results, execTime);

        return results;
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
