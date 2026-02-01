package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.DisputeDTO;
import com.exe202.skillnest.payloads.request.CreateDisputeRequest;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.DisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
@Tag(name = "Dispute", description = "Dispute APIs")
@SecurityRequirement(name = "bearer-jwt")
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    @Operation(summary = "Create new dispute (contract participants only)")
    public ResponseEntity<BaseResponse> createDispute(
            @Valid @RequestBody CreateDisputeRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        DisputeDTO disputeDTO = disputeService.createDispute(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Dispute created successfully", disputeDTO));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my disputes")
    public ResponseEntity<BaseResponse> getMyDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DisputeDTO> disputes = disputeService.getMyDisputes(email, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Disputes retrieved successfully", disputes));
    }

    @GetMapping("/{disputeId}")
    @Operation(summary = "Get dispute by ID (participants only)")
    public ResponseEntity<BaseResponse> getDisputeById(
            @PathVariable Long disputeId,
            Authentication authentication) {
        String email = authentication.getName();
        DisputeDTO disputeDTO = disputeService.getDisputeById(disputeId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Dispute retrieved successfully", disputeDTO));
    }

    @PutMapping("/{disputeId}/status")
    @Operation(summary = "Update dispute status (participants/admin)")
    public ResponseEntity<BaseResponse> updateDisputeStatus(
            @PathVariable Long disputeId,
            @RequestParam String status,
            Authentication authentication) {
        String email = authentication.getName();
        DisputeDTO disputeDTO = disputeService.updateDisputeStatus(disputeId, status, email);
        return ResponseEntity.ok(new BaseResponse(200, "Dispute status updated successfully", disputeDTO));
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get disputes by contract ID (participants only)")
    public ResponseEntity<BaseResponse> getDisputesByContractId(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DisputeDTO> disputes = disputeService.getDisputesByContractId(contractId, email, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Disputes retrieved successfully", disputes));
    }
}

