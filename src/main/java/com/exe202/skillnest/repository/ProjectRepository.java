package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
    Page<Project> findByClientUserId(Long clientId, Pageable pageable);
}

