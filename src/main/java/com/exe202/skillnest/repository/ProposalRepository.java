package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Proposal;
import com.exe202.skillnest.enums.ProposalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    Page<Proposal> findByProjectProjectId(Long projectId, Pageable pageable);
    Page<Proposal> findByStudentUserId(Long studentId, Pageable pageable);
    Page<Proposal> findByProjectProjectIdAndStatus(Long projectId, ProposalStatus status, Pageable pageable);
    Optional<Proposal> findByProjectProjectIdAndStudentUserId(Long projectId, Long studentId);
    boolean existsByProjectProjectIdAndStudentUserId(Long projectId, Long studentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Proposal p WHERE p.proposalId = :id")
    Optional<Proposal> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT p.student.userId FROM Proposal p WHERE p.project.projectId = :projectId")
    List<Long> findStudentIdsByProjectId(@Param("projectId") Long projectId);
}

