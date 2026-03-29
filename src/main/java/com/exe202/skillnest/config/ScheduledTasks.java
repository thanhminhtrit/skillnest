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

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void expireOldPaymentRequests() {
        List<PaymentRequest> expired = paymentRequestRepository
                .findExpiredPendingPayments(LocalDateTime.now());

        for (PaymentRequest pr : expired) {
            pr.setStatus(PaymentStatus.CANCELLED);
            paymentRequestRepository.save(pr);

            Proposal proposal = pr.getProposal();
            if (proposal != null && proposal.getStatus() == ProposalStatus.ACCEPTED) {
                proposal.setStatus(ProposalStatus.SUBMITTED);
                proposalRepository.save(proposal);
            }

            log.info("Auto-cancelled expired payment request: {} (reference: {})",
                    pr.getPaymentRequestId(), pr.getPaymentReference());
        }

        if (!expired.isEmpty()) {
            log.info("Expired {} payment request(s)", expired.size());
        }

        // Also expire subscription payment requests
        List<SubscriptionPaymentRequest> expiredSubs = subPaymentRepo
                .findExpiredPendingPayments(LocalDateTime.now());

        for (SubscriptionPaymentRequest spr : expiredSubs) {
            spr.setStatus(PaymentStatus.CANCELLED);
            subPaymentRepo.save(spr);
            log.info("Auto-cancelled expired subscription payment: {} (reference: {})",
                    spr.getSubPaymentId(), spr.getPaymentReference());
        }

        if (!expiredSubs.isEmpty()) {
            log.info("Expired {} subscription payment(s)", expiredSubs.size());
        }
    }
}
