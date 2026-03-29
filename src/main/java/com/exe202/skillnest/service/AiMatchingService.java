package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.ProjectMatchingResponse;
import com.exe202.skillnest.dto.StudentMatchingResponse;

public interface AiMatchingService {
    StudentMatchingResponse findBestStudents(Long projectId, Long userId, int page, int limit);
    ProjectMatchingResponse findBestProjects(Long userId, int page, int limit);
}
