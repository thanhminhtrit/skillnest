package com.exe202.skillnest.config;

import com.exe202.skillnest.entity.PaymentRequest;
import com.exe202.skillnest.entity.Proposal;
import com.exe202.skillnest.entity.SubscriptionPaymentRequest;
import com.exe202.skillnest.enums.PaymentStatus;
import com.exe202.skillnest.enums.ProposalStatus;
import com.exe202.skillnest.repository.PaymentRequestRepository;
import com.exe202.skillnest.repository.ProposalRepository;
import com.exe202.skillnest.repository.SubscriptionPaymentRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final PaymentRequestRepository paymentRequestRepository;
    private final ProposalRepository proposalRepository;
    private final SubscriptionPaymentRequestRepository subPaymentRepo;
    private final com.exe202.skillnest.service.RatingService ratingService;
    private final com.exe202.skillnest.service.NotificationService notificationService;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void expireOldPaymentRequests() {
        LocalDateTime now = LocalDateTime.now();

        // Expire contract payment requests
        List<PaymentRequest> expired = paymentRequestRepository
                .findExpiredPendingPayments(now);

        for (PaymentRequest pr : expired) {
            pr.setStatus(PaymentStatus.CANCELLED);
            paymentRequestRepository.save(pr);

            Proposal proposal = pr.getProposal();
            if (proposal != null && proposal.getStatus() == ProposalStatus.ACCEPTED) {
                proposal.setStatus(ProposalStatus.SUBMITTED);
                proposalRepository.save(proposal);
            }

            // Detailed log for audit trail and future notification integration
            log.warn("AUTO-EXPIRED payment request: id={}, reference={}, clientId={}, clientName={}, " +
                     "proposalId={}, amount={}, createdAt={}, expiredAt={}. " +
                     "Proposal reset to SUBMITTED. TODO: Send notification to client.",
                    pr.getPaymentRequestId(),
                    pr.getPaymentReference(),
                    pr.getClient() != null ? pr.getClient().getUserId() : "N/A",
                    pr.getClient() != null ? pr.getClient().getFullName() : "N/A",
                    proposal != null ? proposal.getProposalId() : "N/A",
                    pr.getTotalAmount(),
                    pr.getCreatedAt(),
                    pr.getExpiresAt());

            // Notify client about expired payment
            if (pr.getClient() != null) {
                notificationService.notify(
                        pr.getClient().getUserId(),
                        com.exe202.skillnest.enums.NotificationType.PAYMENT_EXPIRED,
                        "Payment expired",
                        "Your payment for reference " + pr.getPaymentReference() + " has expired.",
                        "PAYMENT",
                        pr.getPaymentRequestId()
                );
            }
        }

        if (!expired.isEmpty()) {
            log.info("Total expired contract payment requests: {}", expired.size());
        }

        // Expire subscription payment requests
        List<SubscriptionPaymentRequest> expiredSubs = subPaymentRepo
                .findExpiredPendingPayments(now);

        for (SubscriptionPaymentRequest spr : expiredSubs) {
            spr.setStatus(PaymentStatus.CANCELLED);
            subPaymentRepo.save(spr);

            log.warn("AUTO-EXPIRED subscription payment: id={}, reference={}, userId={}, userName={}, " +
                     "planName={}, amount={}, createdAt={}, expiredAt={}. " +
                     "TODO: Send notification to user.",
                    spr.getSubPaymentId(),
                    spr.getPaymentReference(),
                    spr.getUser() != null ? spr.getUser().getUserId() : "N/A",
                    spr.getUser() != null ? spr.getUser().getFullName() : "N/A",
                    spr.getPlan() != null ? spr.getPlan().getDisplayName() : "N/A",
                    spr.getAmount(),
                    spr.getCreatedAt(),
                    spr.getExpiresAt());
        }

        if (!expiredSubs.isEmpty()) {
            log.info("Total expired subscription payments: {}", expiredSubs.size());
        }
    }

    @Scheduled(fixedRate = 86400000) // Run every 24 hours
    @Transactional
    public void autoRevealExpiredPendingRatings() {
        ratingService.autoRevealExpiredPendingRatings();
    }

    @Scheduled(fixedRate = 86400000) // Run every 24 hours
    @Transactional
    public void autoRateExpiredContracts() {
        ratingService.autoRateExpiredContracts();
    }
}
