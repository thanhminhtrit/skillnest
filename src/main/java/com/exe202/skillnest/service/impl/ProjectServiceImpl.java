package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.CompanyInfoDTO;
import com.exe202.skillnest.dto.ProjectDTO;
import com.exe202.skillnest.entity.CompanyInfo;
import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.entity.Skill;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.ProjectStatus;
import com.exe202.skillnest.enums.ProjectType;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.payloads.request.CreateProjectRequest;
import com.exe202.skillnest.payloads.request.UpdateProjectRequest;
import com.exe202.skillnest.repository.CompanyInfoRepository;
import com.exe202.skillnest.repository.ProjectRepository;
import com.exe202.skillnest.repository.ProposalRepository;
import com.exe202.skillnest.repository.SkillRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.ProjectService;
import com.exe202.skillnest.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CompanyInfoRepository companyInfoRepository;
    private final ProposalRepository proposalRepository;
    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request, String email) {
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check subscription quota before creating post
        subscriptionService.checkAndIncrementPostUsage(client.getUserId());

        Set<Skill> skills = new HashSet<>();
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            skills = request.getSkillIds().stream()
                    .map(skillName -> skillRepository.findByName(skillName)
                            .orElseGet(() -> {
                                // Auto-create skill if not exists
                                Skill newSkill = Skill.builder()
                                        .name(skillName)
                                        .build();
                                return skillRepository.save(newSkill);
                            }))
                    .collect(Collectors.toSet());
        }

        Project project = Project.builder()
                .client(client)
                .title(request.getTitle())
                .description(request.getDescription())
                .projectType(ProjectType.valueOf(request.getProjectType()))
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .currency(request.getCurrency())
                .status(ProjectStatus.OPEN)
                .skills(skills)
                // PREVIOUS NEW FIELDS
                .location(request.getLocation())
                .employmentType(request.getEmploymentType())
                .salaryUnit(request.getSalaryUnit())
                .requirements(request.getRequirements() != null ?
                        new ArrayList<>(request.getRequirements()) : new ArrayList<>())
                // NEW FIELDS FOR RECRUITMENT FORM
                .headcountMin(request.getHeadcountMin())
                .headcountMax(request.getHeadcountMax())
                .deadline(request.getDeadline())
                .benefits(request.getBenefits() != null ?
                        new ArrayList<>(request.getBenefits()) : new ArrayList<>())
                .build();

        project = projectRepository.save(project);
        return convertToDTO(project);
    }

    @Override
    @Transactional
    public ProjectDTO updateProject(Long projectId, UpdateProjectRequest request, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check ownership
        if (!project.getClient().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You can only update your own projects");
        }

        // Update EXISTING fields
        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getProjectType() != null) {
            project.setProjectType(ProjectType.valueOf(request.getProjectType()));
        }
        if (request.getBudgetMin() != null) {
            project.setBudgetMin(request.getBudgetMin());
        }
        if (request.getBudgetMax() != null) {
            project.setBudgetMax(request.getBudgetMax());
        }
        if (request.getCurrency() != null) {
            project.setCurrency(request.getCurrency());
        }
        if (request.getSkillIds() != null) {
            Set<Skill> skills = request.getSkillIds().stream()
                    .map(skillName -> skillRepository.findByName(skillName)
                            .orElseGet(() -> {
                                // Auto-create skill if not exists
                                Skill newSkill = Skill.builder()
                                        .name(skillName)
                                        .build();
                                return skillRepository.save(newSkill);
                            }))
                    .collect(Collectors.toSet());
            project.setSkills(skills);
        }

        // Update NEW fields
        if (request.getLocation() != null) {
            project.setLocation(request.getLocation());
        }
        if (request.getEmploymentType() != null) {
            project.setEmploymentType(request.getEmploymentType());
        }
        if (request.getSalaryUnit() != null) {
            project.setSalaryUnit(request.getSalaryUnit());
        }
        if (request.getRequirements() != null) {
            project.setRequirements(new ArrayList<>(request.getRequirements()));
        }

        // Update RECRUITMENT FORM fields
        if (request.getHeadcountMin() != null) {
            project.setHeadcountMin(request.getHeadcountMin());
        }
        if (request.getHeadcountMax() != null) {
            project.setHeadcountMax(request.getHeadcountMax());
        }
        if (request.getDeadline() != null) {
            project.setDeadline(request.getDeadline());
        }
        if (request.getBenefits() != null) {
            project.setBenefits(new ArrayList<>(request.getBenefits()));
        }

        project = projectRepository.save(project);
        return convertToDTO(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check ownership
        if (!project.getClient().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You can only delete your own projects");
        }

        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectDTO closeProject(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check ownership
        if (!project.getClient().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You can only close your own projects");
        }

        project.setStatus(ProjectStatus.CLOSED);
        project = projectRepository.save(project);
        return convertToDTO(project);
    }

    @Override
    public ProjectDTO getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        return convertToDTO(project);
    }

    @Override
    public Page<ProjectDTO> getAllOpenProjects(Pageable pageable) {
        return projectRepository.findByStatus(ProjectStatus.OPEN, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ProjectDTO> getMyProjects(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return projectRepository.findByClientUserId(user.getUserId(), pageable)
                .map(this::convertToDTO);
    }

    private ProjectDTO convertToDTO(Project project) {
        return convertToDTO(project, null);
    }

    private ProjectDTO convertToDTO(Project project, Long currentUserId) {
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

        // Check if current user has applied (if userId provided)
        Boolean hasApplied = null;
        if (currentUserId != null) {
            hasApplied = proposalRepository.existsByProjectProjectIdAndStudentUserId(
                    project.getProjectId(), currentUserId);
        }

        // TODO: Implement isSaved functionality when SavedProject feature is added
        Boolean isSaved = false;

        return ProjectDTO.builder()
                // ========== EXISTING FIELDS (kept as-is) ==========
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
                // ========== NEW FIELDS ==========
                .location(project.getLocation())
                .employmentType(project.getEmploymentType())
                .salaryUnit(project.getSalaryUnit())
                .requirements(project.getRequirements() != null ?
                        new ArrayList<>(project.getRequirements()) : new ArrayList<>())
                .company(companyInfo)
                .isSaved(isSaved)
                .hasApplied(hasApplied)
                .postedAgo(postedAgo)
                // ========== RECRUITMENT FORM FIELDS ==========
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

        long days = duration.toDays();
        if (days == 0) {
            long hours = duration.toHours();
            if (hours == 0) {
                long minutes = duration.toMinutes();
                return "Đăng " + minutes + " phút trước";
            }
            return "Đăng " + hours + " giờ trước";
        } else if (days == 1) {
            return "Đăng 1 ngày trước";
        } else if (days < 7) {
            return "Đăng " + days + " ngày trước";
        } else if (days < 30) {
            long weeks = days / 7;
            return "Đăng " + weeks + " tuần trước";
        } else if (days < 365) {
            long months = days / 30;
            return "Đăng " + months + " tháng trước";
        } else {
            long years = days / 365;
            return "Đăng " + years + " năm trước";
        }
    }
}
