package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    Page<Dispute> findByContractContractId(Long contractId, Pageable pageable);

    @Query("SELECT d FROM Dispute d WHERE d.disputeId = :disputeId AND (d.contract.client.userId = :userId OR d.contract.student.userId = :userId)")
    Optional<Dispute> findByDisputeIdAndParticipant(Long disputeId, Long userId);

    @Query("SELECT d FROM Dispute d WHERE d.contract.client.userId = :userId OR d.contract.student.userId = :userId")
    Page<Dispute> findByParticipant(Long userId, Pageable pageable);
}

