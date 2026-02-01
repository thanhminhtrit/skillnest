package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.ProjectDTO;
import com.exe202.skillnest.payloads.request.CreateProjectRequest;
import com.exe202.skillnest.payloads.request.UpdateProjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectDTO createProject(CreateProjectRequest request, String email);
    ProjectDTO updateProject(Long projectId, UpdateProjectRequest request, String email);
    void deleteProject(Long projectId, String email);
    ProjectDTO closeProject(Long projectId, String email);
    ProjectDTO getProjectById(Long projectId);
    Page<ProjectDTO> getAllOpenProjects(Pageable pageable);
    Page<ProjectDTO> getMyProjects(String email, Pageable pageable);
}
