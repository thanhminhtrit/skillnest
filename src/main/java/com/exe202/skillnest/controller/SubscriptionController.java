package com.exe202.skillnest.controller;

import com.exe202.skillnest.config.security.IsManager;
import com.exe202.skillnest.dto.SubscriptionDTO;
import com.exe202.skillnest.dto.SubscriptionPaymentRequestDTO;
import com.exe202.skillnest.dto.SubscriptionPaymentResponse;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.SubscriptionService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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

    @PostMapping("/initiate")
    @Operation(summary = "Buy a subscription plan — FREE activates immediately, paid plans return payment info",
               description = "Both CLIENT and STUDENT can buy subscription plans. " +
                       "FREE plan is activated instantly. " +
                       "Paid plans (BASIC, PRO) return QR code and bank details for payment. " +
                       "After bank transfer, admin will verify and activate the plan.",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> initiateSubscription(@RequestParam Long planId) {
        Long userId = securityUtil.getCurrentUserId();
        SubscriptionPaymentResponse response = subscriptionService.initiateSubscription(userId, planId);

        if (response.getPrice() != null && response.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return ResponseEntity.ok(new BaseResponse(200, "Free plan activated!", response));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Payment info generated. Please transfer to activate plan.", response));
    }

    @PostMapping("/subscribe")
    @IsManager
    @Operation(summary = "Admin manual activate subscription (ADMIN/MANAGER only)",
               description = "Use POST /api/subscriptions/initiate for normal subscription flow. This endpoint is for admin manual activation only.",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> subscribe(
            @RequestParam Long userId,
            @RequestParam Long planId) {
        return ResponseEntity.ok(new BaseResponse(200, "Subscription activated by admin",
                subscriptionService.subscribe(userId, planId)));
    }

    @PostMapping("/verify")
    @IsManager
    @Operation(summary = "Verify subscription payment and activate plan (ADMIN/MANAGER)",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> verifySubscriptionPayment(
            @RequestParam String paymentReference) {
        Long verifierId = securityUtil.getCurrentUserId();
        SubscriptionDTO result = subscriptionService.verifySubscriptionPayment(paymentReference, verifierId);
        return ResponseEntity.ok(new BaseResponse(200, "Subscription activated successfully", result));
    }

    @GetMapping("/pending")
    @IsManager
    @Operation(summary = "Get pending subscription payments (ADMIN/MANAGER)",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<BaseResponse> getPendingSubscriptionPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SubscriptionPaymentRequestDTO> payments =
                subscriptionService.getPendingSubscriptionPayments(pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Pending subscription payments retrieved", payments));
    }
}
