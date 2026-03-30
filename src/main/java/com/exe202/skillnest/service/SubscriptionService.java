package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.SubscriptionDTO;
import com.exe202.skillnest.dto.SubscriptionPaymentRequestDTO;
import com.exe202.skillnest.dto.SubscriptionPaymentResponse;
import com.exe202.skillnest.dto.SubscriptionPlanDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionService {
    SubscriptionDTO getMySubscription(Long userId);
    List<SubscriptionPlanDTO> getAllPlans();
    SubscriptionDTO subscribe(Long userId, Long planId);
    void checkAndIncrementPostUsage(Long userId);
    void checkAndIncrementAiMatchingUsage(Long userId);
    void checkAndIncrementInviteUsage(Long userId);

    /**
     * Initiate subscription payment — returns QR code for payment.
     * FREE plan is activated immediately, paid plans require payment verification.
     */
    SubscriptionPaymentResponse initiateSubscription(Long userId, Long planId);

    /**
     * Verify subscription payment and activate plan (ADMIN/MANAGER).
     */
    SubscriptionDTO verifySubscriptionPayment(String paymentReference, Long verifierId);

    Page<SubscriptionPaymentRequestDTO> getPendingSubscriptionPayments(Pageable pageable);
}
