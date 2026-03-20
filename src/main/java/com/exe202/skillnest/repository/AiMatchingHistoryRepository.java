package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.AiMatchingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiMatchingHistoryRepository extends JpaRepository<AiMatchingHistory, Long> {
    Page<AiMatchingHistory> findByUser_UserId(Long userId, Pageable pageable);
}
