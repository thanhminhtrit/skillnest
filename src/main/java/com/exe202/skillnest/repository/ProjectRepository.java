package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
    Page<Project> findByClientUserId(Long clientId, Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Project p
            JOIN p.skills s
            WHERE p.status = 'OPEN'
            AND LOWER(s.name) IN :skillNames
            """)
    List<Project> findOpenProjectsBySkills(@Param("skillNames") List<String> skillNames);

    @Query("SELECT p FROM Project p WHERE p.status = 'OPEN'")
    List<Project> findAllOpenProjects();
}

