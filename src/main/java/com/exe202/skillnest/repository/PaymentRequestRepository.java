package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.PaymentRequest;
import com.exe202.skillnest.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {

    Optional<PaymentRequest> findByProposal_ProposalId(Long proposalId);

    Optional<PaymentRequest> findByPaymentReference(String paymentReference);

    Page<PaymentRequest> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.client.userId = :clientId")
    Page<PaymentRequest> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    boolean existsByPaymentReference(String paymentReference);

    @Query("""
            SELECT pr FROM PaymentRequest pr
            JOIN FETCH pr.client
            JOIN FETCH pr.proposal p
            JOIN FETCH p.student
            JOIN FETCH p.project
            WHERE pr.paymentRequestId = :id
            """)
    Optional<PaymentRequest> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT pr FROM PaymentRequest pr
            JOIN FETCH pr.client
            JOIN FETCH pr.proposal p
            JOIN FETCH p.student
            WHERE pr.proposal.proposalId = :proposalId
            """)
    Optional<PaymentRequest> findByProposalIdWithDetails(@Param("proposalId") Long proposalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.paymentRequestId = :id")
    Optional<PaymentRequest> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status = 'PENDING_PAYMENT' AND pr.expiresAt IS NOT NULL AND pr.expiresAt < :now")
    List<PaymentRequest> findExpiredPendingPayments(@Param("now") LocalDateTime now);
}

