package com.exe202.skillnest.controller;

import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.SubscriptionService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SecurityUtil securityUtil;

    @GetMapping("/plans")
    public ResponseEntity<BaseResponse> getAllPlans() {
        return ResponseEntity.ok(new BaseResponse(200, "Success",
                subscriptionService.getAllPlans()));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my active subscription", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> getMySubscription() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(new BaseResponse(200, "Success",
                subscriptionService.getMySubscription(userId)));
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to a plan", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> subscribe(@RequestParam Long planId) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(new BaseResponse(200, "Subscribed",
                subscriptionService.subscribe(userId, planId)));
    }
}
