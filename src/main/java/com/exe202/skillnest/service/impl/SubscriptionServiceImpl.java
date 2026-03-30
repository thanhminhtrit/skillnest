package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.SubscriptionDTO;
import com.exe202.skillnest.dto.SubscriptionPaymentRequestDTO;
import com.exe202.skillnest.dto.SubscriptionPaymentResponse;
import com.exe202.skillnest.dto.SubscriptionPlanDTO;
import com.exe202.skillnest.entity.SubscriptionPaymentRequest;
import com.exe202.skillnest.entity.SubscriptionPlan;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.entity.UserSubscription;
import com.exe202.skillnest.enums.PaymentStatus;
import com.exe202.skillnest.enums.SubscriptionStatus;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.repository.SubscriptionPaymentRequestRepository;
import com.exe202.skillnest.repository.SubscriptionPlanRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.repository.UserSubscriptionRepository;
import com.exe202.skillnest.service.FileStorageService;
import com.exe202.skillnest.service.SubscriptionService;
import com.exe202.skillnest.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserSubscriptionRepository subRepo;
    private final SubscriptionPlanRepository planRepo;
    private final UserRepository userRepo;
    private final SubscriptionPaymentRequestRepository subPaymentRepo;
    private final QRCodeGenerator qrCodeGenerator;
    private final FileStorageService fileStorageService;

    @Value("${payment.bank.name:Vietcombank}")
    private String bankName;

    @Value("${payment.bank.account-number:1234567890}")
    private String bankAccountNumber;

    @Value("${payment.bank.account-name:SKILLNEST PLATFORM}")
    private String bankAccountName;

    @Value("${payment.bank.code:VCB}")
    private String bankCode;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDTO getMySubscription(Long userId) {
        UserSubscription sub = subRepo.findActiveByUserId(userId)
                .orElseThrow(() -> new NotFoundException("No active subscription found"));
        return toDTO(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanDTO> getAllPlans() {
        return planRepo.findAll().stream()
                .filter(SubscriptionPlan::getIsActive)
                .map(this::toPlanDTO)
                .toList();
    }

    @Override
    public SubscriptionDTO subscribe(Long userId, Long planId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        SubscriptionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new NotFoundException("Subscription plan not found"));

        if (!plan.getIsActive()) {
            throw new BadRequestException("This subscription plan is no longer available");
        }

        // Cancel any existing active subscription
        subRepo.findActiveByUserId(userId).ifPresent(existing -> {
            existing.setStatus(SubscriptionStatus.CANCELLED);
            subRepo.save(existing);
        });

        LocalDateTime now = LocalDateTime.now();
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .startDate(now)
                .endDate(now.plusDays(plan.getDurationDays()))
                .status(SubscriptionStatus.ACTIVE)
                .postsUsed(0)
                .aiMatchingUsed(0)
                .autoRenew(true)
                .build();

        return toDTO(subRepo.save(subscription));
    }

    @Override
    public void checkAndIncrementPostUsage(Long userId) {
        UserSubscription sub = subRepo.findActiveByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No active subscription"));

        if (!sub.canCreatePost()) {
            throw new BadRequestException(
                    "Post limit reached. Used: " + sub.getPostsUsed() +
                    "/" + (sub.getPlan().getPostLimit() == null ? "∞" : sub.getPlan().getPostLimit())
            );
        }

        sub.incrementPostUsage();
        subRepo.save(sub);
    }

    @Override
    public void checkAndIncrementAiMatchingUsage(Long userId) {
        UserSubscription sub = subRepo.findActiveByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No active subscription"));

        if (!sub.canUseAiMatching()) {
            throw new BadRequestException("AI matching limit reached. Upgrade your plan!");
        }

        sub.incrementAiMatchingUsage();
        subRepo.save(sub);
    }

    @Override
    public void checkAndIncrementInviteUsage(Long userId) {
        UserSubscription sub = subRepo.findActiveByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No active subscription"));

        if (!sub.canInvite()) {
            throw new BadRequestException(
                    "Invite limit reached. Used: " + sub.getInvitesUsed() +
                    "/" + (sub.getPlan().getInviteLimit() == null ? "∞" : sub.getPlan().getInviteLimit()) +
                    ". Upgrade your plan for more invites!"
            );
        }

        sub.incrementInviteUsage();
        subRepo.save(sub);
    }

    @Override
    public SubscriptionPaymentResponse initiateSubscription(Long userId, Long planId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        SubscriptionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));

        if (!plan.getIsActive()) {
            throw new BadRequestException("This plan is no longer available");
        }

        // FREE plan — activate immediately
        if (plan.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            subscribe(userId, planId);
            return SubscriptionPaymentResponse.builder()
                    .planId(planId)
                    .planName(plan.getDisplayName())
                    .price(BigDecimal.ZERO)
                    .message("Free plan activated successfully! No payment required.")
                    .build();
        }

        // Paid plan — generate unique payment reference
        String paymentReference;
        do {
            paymentReference = "SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (subPaymentRepo.existsByPaymentReference(paymentReference));

        String transferNote = "SKILLNEST " + paymentReference;

        // Generate QR code
        String qrCodeUrl = null;
        String qrCodeBase64 = null;
        try {
            byte[] qrBytes = qrCodeGenerator.generateBankTransferQR(
                    bankCode, bankAccountNumber, bankAccountName,
                    plan.getPrice(), transferNote);
            qrCodeUrl = fileStorageService.storeBytes(qrBytes, paymentReference + ".png", "qr-codes");
            qrCodeBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            log.error("Failed to generate QR code for subscription", e);
        }

        // Save subscription payment request so admin can see it in pending payments
        SubscriptionPaymentRequest subPayment = SubscriptionPaymentRequest.builder()
                .user(user)
                .plan(plan)
                .amount(plan.getPrice())
                .status(PaymentStatus.PENDING_PAYMENT)
                .paymentReference(paymentReference)
                .qrCodeUrl(qrCodeUrl)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        subPaymentRepo.save(subPayment);

        log.info("Subscription payment request created: {} for user {} plan {}",
                paymentReference, userId, plan.getDisplayName());

        return SubscriptionPaymentResponse.builder()
                .subPaymentId(subPayment.getSubPaymentId())
                .planId(planId)
                .planName(plan.getDisplayName())
                .price(plan.getPrice())
                .qrCodeUrl(qrCodeUrl)
                .qrCodeBase64(qrCodeBase64)
                .paymentReference(paymentReference)
                .bankDetails(SubscriptionPaymentResponse.BankDetails.builder()
                        .bankName(bankName)
                        .accountNumber(bankAccountNumber)
                        .accountName(bankAccountName)
                        .transferNote(transferNote)
                        .build())
                .message(String.format(
                        "Please transfer %s VND to activate %s. " +
                        "Transfer note: %s. " +
                        "Your plan will be activated after admin verification.",
                        plan.getPrice().toPlainString(),
                        plan.getDisplayName(),
                        transferNote))
                .build();
    }

    @Override
    public SubscriptionDTO verifySubscriptionPayment(String paymentReference, Long verifierId) {
        SubscriptionPaymentRequest subPayment = subPaymentRepo.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new NotFoundException("Subscription payment not found: " + paymentReference));

        if (subPayment.getStatus() != PaymentStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Payment is not in PENDING_PAYMENT status");
        }

        User verifier = userRepo.findById(verifierId)
                .orElseThrow(() -> new NotFoundException("Verifier not found"));

        subPayment.setStatus(PaymentStatus.PAID);
        subPayment.setVerifiedBy(verifier);
        subPayment.setVerifiedAt(LocalDateTime.now());
        subPaymentRepo.save(subPayment);

        return subscribe(subPayment.getUser().getUserId(), subPayment.getPlan().getPlanId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionPaymentRequestDTO> getPendingSubscriptionPayments(Pageable pageable) {
        return subPaymentRepo.findByStatus(PaymentStatus.PENDING_PAYMENT, pageable)
                .map(this::toSubPaymentDTO);
    }

    private SubscriptionPaymentRequestDTO toSubPaymentDTO(SubscriptionPaymentRequest entity) {
        return SubscriptionPaymentRequestDTO.builder()
                .subPaymentId(entity.getSubPaymentId())
                .userId(entity.getUser().getUserId())
                .userName(entity.getUser().getFullName())
                .planName(entity.getPlan().getDisplayName())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .paymentReference(entity.getPaymentReference())
                .qrCodeUrl(entity.getQrCodeUrl())
                .verifiedBy(entity.getVerifiedBy() != null ? entity.getVerifiedBy().getUserId() : null)
                .verifiedByName(entity.getVerifiedBy() != null ? entity.getVerifiedBy().getFullName() : null)
                .verifiedAt(entity.getVerifiedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private SubscriptionDTO toDTO(UserSubscription sub) {
        SubscriptionPlan plan = sub.getPlan();
        Integer postLimit = plan.getPostLimit();
        Integer aiLimit = plan.getAiMatchingLimit();

        return SubscriptionDTO.builder()
                .subscriptionId(sub.getSubscriptionId())
                .planName(plan.getDisplayName())
                .price(plan.getPrice())
                .postLimit(postLimit)
                .postsUsed(sub.getPostsUsed())
                .postsRemaining(postLimit == null ? null : postLimit - sub.getPostsUsed())
                .aiMatchingLimit(aiLimit)
                .aiMatchingUsed(sub.getAiMatchingUsed())
                .aiMatchingRemaining(aiLimit == null ? null : aiLimit - sub.getAiMatchingUsed())
                .inviteLimit(plan.getInviteLimit())
                .invitesUsed(sub.getInvitesUsed())
                .invitesRemaining(plan.getInviteLimit() == null ? null : plan.getInviteLimit() - sub.getInvitesUsed())
                .endDate(sub.getEndDate())
                .status(sub.getStatus().name())
                .build();
    }

    private SubscriptionPlanDTO toPlanDTO(SubscriptionPlan plan) {
        return SubscriptionPlanDTO.builder()
                .planId(plan.getPlanId())
                .name(plan.getName())
                .displayName(plan.getDisplayName())
                .price(plan.getPrice())
                .postLimit(plan.getPostLimit())
                .aiMatchingLimit(plan.getAiMatchingLimit())
                .inviteLimit(plan.getInviteLimit())
                .durationDays(plan.getDurationDays())
                .build();
    }
}
