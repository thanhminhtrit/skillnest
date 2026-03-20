package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.SubscriptionDTO;
import com.exe202.skillnest.dto.SubscriptionPlanDTO;

import java.util.List;

public interface SubscriptionService {
    SubscriptionDTO getMySubscription(Long userId);
    List<SubscriptionPlanDTO> getAllPlans();
    SubscriptionDTO subscribe(Long userId, Long planId);
    void checkAndIncrementPostUsage(Long userId);
    void checkAndIncrementAiMatchingUsage(Long userId);
}
