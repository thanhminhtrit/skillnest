package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.ProjectDTO;
import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.entity.SavedProject;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.repository.ProjectRepository;
import com.exe202.skillnest.repository.SavedProjectRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.ProjectService;
import com.exe202.skillnest.service.SavedProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedProjectServiceImpl implements SavedProjectService {

    private final SavedProjectRepository savedProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

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
        // Use a helper method in ProjectService or create our own conversion
        return savedProjects.map(savedProject -> {
            Project project = savedProject.getProject();
            // For now, use the simple getProjectById which doesn't include user-specific info
            return projectService.getProjectById(project.getProjectId());
        });
    }

    @Override
    public boolean isProjectSaved(Long projectId, Long userId) {
        if (userId == null || projectId == null) {
            return false;
        }
        return savedProjectRepository.existsByUserUserIdAndProjectProjectId(userId, projectId);
    }
}
