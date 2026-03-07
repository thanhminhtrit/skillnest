package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.CompanyInfoDTO;
import com.exe202.skillnest.dto.ProjectDTO;
import com.exe202.skillnest.entity.CompanyInfo;
import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.entity.SavedProject;
import com.exe202.skillnest.entity.Skill;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.repository.CompanyInfoRepository;
import com.exe202.skillnest.repository.ProjectRepository;
import com.exe202.skillnest.repository.ProposalRepository;
import com.exe202.skillnest.repository.SavedProjectRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.SavedProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedProjectServiceImpl implements SavedProjectService {

    private final SavedProjectRepository savedProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CompanyInfoRepository companyInfoRepository;
    private final ProposalRepository proposalRepository;

    @Override
    @Transactional
    public String saveProject(Long projectId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        // Check if already saved
        if (savedProjectRepository.existsByUserUserIdAndProjectProjectId(user.getUserId(), projectId)) {
            return "Project already saved";
        }

        SavedProject savedProject = SavedProject.builder()
                .user(user)
                .project(project)
                .build();

        savedProjectRepository.save(savedProject);
        return "Project saved successfully";
    }

    @Override
    @Transactional
    public String unsaveProject(Long projectId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!savedProjectRepository.existsByUserUserIdAndProjectProjectId(user.getUserId(), projectId)) {
            throw new NotFoundException("Saved project not found");
        }

        savedProjectRepository.deleteByUserUserIdAndProjectProjectId(user.getUserId(), projectId);
        return "Project unsaved successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getSavedProjects(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Page<SavedProject> savedProjects = savedProjectRepository.findByUserUserId(user.getUserId(), pageable);
        
        // Convert saved projects to project DTOs
        // Build DTOs directly to avoid N+1 query issue
        return savedProjects.map(savedProject -> 
            convertToProjectDTO(savedProject.getProject(), user.getUserId())
        );
    }

    @Override
    public boolean isProjectSaved(Long projectId, Long userId) {
        if (userId == null || projectId == null) {
            return false;
        }
        return savedProjectRepository.existsByUserUserIdAndProjectProjectId(userId, projectId);
    }

    /**
     * Convert Project entity to ProjectDTO
     * This is a simplified version to avoid circular dependency with ProjectService
     */
    private ProjectDTO convertToProjectDTO(Project project, Long currentUserId) {
        Set<String> skillNames = project.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        // Get company info for the client
        CompanyInfoDTO companyInfo = null;
        CompanyInfo companyInfoEntity = companyInfoRepository
                .findByUser_UserId(project.getClient().getUserId())
                .orElse(null);

        if (companyInfoEntity != null) {
            companyInfo = CompanyInfoDTO.builder()
                    .name(companyInfoEntity.getName())
                    .location(companyInfoEntity.getLocation())
                    .size(companyInfoEntity.getSize())
                    .industry(companyInfoEntity.getIndustry())
                    .build();
        } else {
            // Fallback: use client name as company name
            companyInfo = CompanyInfoDTO.builder()
                    .name(project.getClient().getFullName())
                    .location(project.getLocation())
                    .size(null)
                    .industry(null)
                    .build();
        }

        // Calculate "posted ago" string
        String postedAgo = calculatePostedAgo(project.getCreatedAt());

        // Check if current user has applied
        Boolean hasApplied = proposalRepository.existsByProjectProjectIdAndStudentUserId(
                project.getProjectId(), currentUserId);

        // For saved projects, isSaved is always true
        Boolean isSaved = true;

        return ProjectDTO.builder()
                .projectId(project.getProjectId())
                .clientId(project.getClient().getUserId())
                .clientName(project.getClient().getFullName())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectType(project.getProjectType().name())
                .budgetMin(project.getBudgetMin())
                .budgetMax(project.getBudgetMax())
                .currency(project.getCurrency())
                .status(project.getStatus().name())
                .skills(skillNames)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .location(project.getLocation())
                .employmentType(project.getEmploymentType())
                .salaryUnit(project.getSalaryUnit())
                .requirements(project.getRequirements() != null ?
                        new ArrayList<>(project.getRequirements()) : new ArrayList<>())
                .company(companyInfo)
                .isSaved(isSaved)
                .hasApplied(hasApplied)
                .postedAgo(postedAgo)
                .headcountMin(project.getHeadcountMin())
                .headcountMax(project.getHeadcountMax())
                .deadline(project.getDeadline())
                .benefits(project.getBenefits() != null ?
                        new ArrayList<>(project.getBenefits()) : new ArrayList<>())
                .build();
    }

    private String calculatePostedAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return "Posted just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return String.format("Posted %d %s ago", minutes, minutes == 1 ? "minute" : "minutes");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return String.format("Posted %d %s ago", hours, hours == 1 ? "hour" : "hours");
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return String.format("Posted %d %s ago", days, days == 1 ? "day" : "days");
        } else if (seconds < 2592000) {
            long weeks = seconds / 604800;
            return String.format("Posted %d %s ago", weeks, weeks == 1 ? "week" : "weeks");
        } else {
            long months = seconds / 2592000;
            return String.format("Posted %d %s ago", months, months == 1 ? "month" : "months");
        }
    }
}
