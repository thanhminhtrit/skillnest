package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Page<Contract> findByClientUserIdOrStudentUserId(Long clientId, Long studentId, Pageable pageable);
    Optional<Contract> findByProposalProposalId(Long proposalId);

    @Query("SELECT c FROM Contract c WHERE c.contractId = :contractId AND (c.client.userId = :userId OR c.student.userId = :userId)")
    Optional<Contract> findByContractIdAndParticipant(Long contractId, Long userId);
}

