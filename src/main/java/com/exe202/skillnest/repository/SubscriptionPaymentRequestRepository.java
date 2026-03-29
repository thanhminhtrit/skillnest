package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.SubscriptionPaymentRequest;
import com.exe202.skillnest.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPaymentRequestRepository extends JpaRepository<SubscriptionPaymentRequest, Long> {
    Page<SubscriptionPaymentRequest> findByStatus(PaymentStatus status, Pageable pageable);

    Optional<SubscriptionPaymentRequest> findByPaymentReference(String paymentReference);

    @Query("SELECT spr FROM SubscriptionPaymentRequest spr WHERE spr.status = 'PENDING_PAYMENT' AND spr.expiresAt IS NOT NULL AND spr.expiresAt < :now")
    List<SubscriptionPaymentRequest> findExpiredPendingPayments(@Param("now") LocalDateTime now);

    boolean existsByPaymentReference(String paymentReference);
}
