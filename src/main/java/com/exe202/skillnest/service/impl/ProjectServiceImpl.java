package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.ProjectDTO;
import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.entity.Skill;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.ProjectStatus;
import com.exe202.skillnest.enums.ProjectType;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.payloads.request.CreateProjectRequest;
import com.exe202.skillnest.payloads.request.UpdateProjectRequest;
import com.exe202.skillnest.repository.ProjectRepository;
import com.exe202.skillnest.repository.SkillRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request, String email) {
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Set<Skill> skills = new HashSet<>();
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            skills = request.getSkillIds().stream()
                    .map(skillId -> skillRepository.findById(skillId)
                            .orElseThrow(() -> new NotFoundException("Skill not found with id: " + skillId)))
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

        // Update fields
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
                    .map(skillId -> skillRepository.findById(skillId)
                            .orElseThrow(() -> new NotFoundException("Skill not found with id: " + skillId)))
                    .collect(Collectors.toSet());
            project.setSkills(skills);
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
        Set<String> skillNames = project.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

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
                .build();
    }
}

