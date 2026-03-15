package com.exe202.skillnest.controller;

import com.exe202.skillnest.config.security.IsAdmin;
import com.exe202.skillnest.config.security.IsClient;
import com.exe202.skillnest.config.security.IsManager;
import com.exe202.skillnest.dto.PaymentAcceptanceResponse;
import com.exe202.skillnest.dto.PaymentRequestDTO;
import com.exe202.skillnest.dto.TransactionDTO;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.PaymentService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment escrow system with 8% platform fee")
@SecurityRequirement(name = "bearer-jwt")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtil securityUtil;

    @PostMapping("/proposals/{proposalId}/accept")
    @IsClient
    @Operation(summary = "Accept proposal and initiate payment (CLIENT only)")
    public ResponseEntity<BaseResponse> acceptProposalWithPayment(@PathVariable Long proposalId) {
        Long clientId = securityUtil.getCurrentUserId();
        PaymentAcceptanceResponse response = paymentService.acceptProposalWithPayment(proposalId, clientId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Payment request created. Please complete bank transfer.", response));
    }

    @PostMapping("/{paymentRequestId}/verify")
    @IsManager
    @Operation(summary = "Verify payment and create contract (ADMIN/MANAGER only)")
    public ResponseEntity<BaseResponse> verifyPayment(@PathVariable Long paymentRequestId) {
        Long verifierId = securityUtil.getCurrentUserId();
        PaymentRequestDTO response = paymentService.verifyPayment(paymentRequestId, verifierId);
        return ResponseEntity.ok(new BaseResponse(200, "Payment verified and contract created successfully", response));
    }

    @PostMapping("/contracts/{contractId}/release")
    @IsAdmin
    @Operation(summary = "Release payment to student after completion (ADMIN only)")
    public ResponseEntity<BaseResponse> releasePaymentToStudent(@PathVariable Long contractId) {
        Long adminId = securityUtil.getCurrentUserId();
        TransactionDTO transaction = paymentService.releasePaymentToStudent(contractId, adminId);
        return ResponseEntity.ok(new BaseResponse(200, "Payment released to student successfully", transaction));
    }

    @PostMapping("/contracts/{contractId}/refund")
    @IsAdmin
    @Operation(summary = "Refund payment to client (ADMIN only)")
    public ResponseEntity<BaseResponse> refundPaymentToClient(
            @PathVariable Long contractId,
            @RequestParam(required = false) String reason) {
        Long adminId = securityUtil.getCurrentUserId();
        TransactionDTO transaction = paymentService.refundPaymentToClient(contractId, adminId, reason);
        return ResponseEntity.ok(new BaseResponse(200, "Payment refunded to client successfully", transaction));
    }

    @GetMapping("/pending")
    @IsManager
    @Operation(summary = "Get all pending payment requests (ADMIN/MANAGER only)")
    public ResponseEntity<BaseResponse> getPendingPayments(Pageable pageable) {
        Page<PaymentRequestDTO> payments = paymentService.getPendingPayments(pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Pending payments retrieved successfully", payments));
    }

    @GetMapping("/{paymentRequestId}")
    @Operation(summary = "Get payment request details")
    public ResponseEntity<BaseResponse> getPaymentRequestById(@PathVariable Long paymentRequestId) {
        PaymentRequestDTO payment = paymentService.getPaymentRequestById(paymentRequestId);
        return ResponseEntity.ok(new BaseResponse(200, "Payment request retrieved successfully", payment));
    }

    @GetMapping("/proposals/{proposalId}")
    @Operation(summary = "Get payment request by proposal ID")
    public ResponseEntity<BaseResponse> getPaymentRequestByProposalId(@PathVariable Long proposalId) {
        PaymentRequestDTO payment = paymentService.getPaymentRequestByProposalId(proposalId);
        return ResponseEntity.ok(new BaseResponse(200, "Payment request retrieved successfully", payment));
    }

    @DeleteMapping("/{paymentRequestId}")
    @IsClient
    @Operation(summary = "Cancel payment request before verification (CLIENT only)")
    public ResponseEntity<BaseResponse> cancelPaymentRequest(@PathVariable Long paymentRequestId) {
        Long clientId = securityUtil.getCurrentUserId();
        paymentService.cancelPaymentRequest(paymentRequestId, clientId);
        return ResponseEntity.ok(new BaseResponse(200, "Payment request cancelled successfully", null));
    }

    @GetMapping("/contracts/{contractId}/transactions")
    @Operation(summary = "Get all transactions for a contract")
    public ResponseEntity<BaseResponse> getContractTransactions(
            @PathVariable Long contractId,
            Pageable pageable) {
        Page<TransactionDTO> transactions = paymentService.getContractTransactions(contractId, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Contract transactions retrieved successfully", transactions));
    }

    @GetMapping("/transactions")
    @IsAdmin
    @Operation(summary = "Get all transactions (ADMIN only)")
    public ResponseEntity<BaseResponse> getAllTransactions(Pageable pageable) {
        Page<TransactionDTO> transactions = paymentService.getAllTransactions(pageable);
        return ResponseEntity.ok(new BaseResponse(200, "All transactions retrieved successfully", transactions));
    }
}
