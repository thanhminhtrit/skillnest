package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.ProjectDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SavedProjectService {
    
    /**
     * Save a project for a user
     * @param projectId ID of the project to save
     * @param email Email of the user saving the project
     * @return Success message
     */
    String saveProject(Long projectId, String email);
    
    /**
     * Unsave a project for a user
     * @param projectId ID of the project to unsave
     * @param email Email of the user unsaving the project
     * @return Success message
     */
    String unsaveProject(Long projectId, String email);
    
    /**
     * Get all saved projects for a user
     * @param email Email of the user
     * @param pageable Pagination information
     * @return Page of saved projects
     */
    Page<ProjectDTO> getSavedProjects(String email, Pageable pageable);
    
    /**
     * Check if a project is saved by a user
     * @param projectId ID of the project
     * @param userId ID of the user
     * @return true if saved, false otherwise
     */
    boolean isProjectSaved(Long projectId, Long userId);
}
