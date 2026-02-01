package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Proposal;
import com.exe202.skillnest.enums.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    Page<Proposal> findByProjectProjectId(Long projectId, Pageable pageable);
    Page<Proposal> findByStudentUserId(Long studentId, Pageable pageable);
    Page<Proposal> findByProjectProjectIdAndStatus(Long projectId, ProposalStatus status, Pageable pageable);
    Optional<Proposal> findByProjectProjectIdAndStudentUserId(Long projectId, Long studentId);
    boolean existsByProjectProjectIdAndStudentUserId(Long projectId, Long studentId);
}

