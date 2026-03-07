package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.SavedProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedProjectRepository extends JpaRepository<SavedProject, Long> {
    
    /**
     * Check if a user has saved a specific project
     */
    boolean existsByUserUserIdAndProjectProjectId(Long userId, Long projectId);
    
    /**
     * Find a saved project by user and project
     */
    Optional<SavedProject> findByUserUserIdAndProjectProjectId(Long userId, Long projectId);
    
    /**
     * Get all saved projects for a user with eager loading to avoid N+1 queries
     */
    @Query("SELECT sp FROM SavedProject sp " +
           "LEFT JOIN FETCH sp.project p " +
           "LEFT JOIN FETCH p.client c " +
           "LEFT JOIN FETCH p.skills " +
           "WHERE sp.user.userId = :userId")
    Page<SavedProject> findByUserUserIdWithProject(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get all saved projects for a user (simple version without joins)
     */
    Page<SavedProject> findByUserUserId(Long userId, Pageable pageable);
    
    /**
     * Delete a saved project by user and project
     */
    void deleteByUserUserIdAndProjectProjectId(Long userId, Long projectId);
}
