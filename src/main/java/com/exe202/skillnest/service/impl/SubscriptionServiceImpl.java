package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.SubscriptionDTO;
import com.exe202.skillnest.dto.SubscriptionPlanDTO;
import com.exe202.skillnest.entity.SubscriptionPlan;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.entity.UserSubscription;
import com.exe202.skillnest.enums.SubscriptionStatus;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.repository.SubscriptionPlanRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.repository.UserSubscriptionRepository;
import com.exe202.skillnest.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserSubscriptionRepository subRepo;
    private final SubscriptionPlanRepository planRepo;
    private final UserRepository userRepo;

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
                .durationDays(plan.getDurationDays())
                .build();
    }
}
