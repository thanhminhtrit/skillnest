package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.MatchingResultDTO;

import java.util.List;

public interface AiMatchingService {
    List<MatchingResultDTO> findBestStudents(Long projectId, Long userId, int limit);
    List<MatchingResultDTO> findBestProjects(Long userId, int limit);
}
