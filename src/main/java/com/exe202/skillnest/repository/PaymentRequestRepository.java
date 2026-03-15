package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.PaymentRequest;
import com.exe202.skillnest.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {

    Optional<PaymentRequest> findByProposal_ProposalId(Long proposalId);

    Optional<PaymentRequest> findByPaymentReference(String paymentReference);

    Page<PaymentRequest> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.client.userId = :clientId")
    Page<PaymentRequest> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    boolean existsByPaymentReference(String paymentReference);
}

