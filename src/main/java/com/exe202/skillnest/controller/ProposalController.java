package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.ProposalDTO;
import com.exe202.skillnest.payloads.request.CreateProposalRequest;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.ProposalService;
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
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@Tag(name = "Proposal", description = "Proposal APIs")
@SecurityRequirement(name = "bearer-jwt")
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping
    @Operation(summary = "Create new proposal (STUDENT only)")
    public ResponseEntity<BaseResponse> createProposal(
            @Valid @RequestBody CreateProposalRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ProposalDTO proposalDTO = proposalService.createProposal(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Proposal created successfully", proposalDTO));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all proposals for a project (project owner only)")
    public ResponseEntity<BaseResponse> getProposalsForProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProposalDTO> proposals = proposalService.getProposalsForProject(projectId, email, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Proposals retrieved successfully", proposals));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my proposals (STUDENT)")
    public ResponseEntity<BaseResponse> getMyProposals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProposalDTO> proposals = proposalService.getMyProposals(email, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Proposals retrieved successfully", proposals));
    }

    @GetMapping("/{proposalId}")
    @Operation(summary = "Get proposal by ID")
    public ResponseEntity<BaseResponse> getProposalById(
            @PathVariable Long proposalId,
            Authentication authentication) {
        String email = authentication.getName();
        ProposalDTO proposalDTO = proposalService.getProposalById(proposalId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Proposal retrieved successfully", proposalDTO));
    }

    @PostMapping("/{proposalId}/accept")
    @Deprecated
    @Operation(
        summary = "DEPRECATED - Use /api/payments/proposals/{proposalId}/accept instead",
        description = "⚠️ This endpoint is deprecated and will be removed in future version. Please use the Payment API to accept proposals with escrow payment protection.",
        deprecated = true
    )
    public ResponseEntity<BaseResponse> acceptProposal(
            @PathVariable Long proposalId,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new BaseResponse(
                    410,
                    "This endpoint is deprecated. Please use POST /api/payments/proposals/{proposalId}/accept for secure payment flow with escrow protection.",
                    null
                ));
    }

    @PostMapping("/{proposalId}/reject")
    @Operation(summary = "Reject proposal (project owner only)")
    public ResponseEntity<BaseResponse> rejectProposal(
            @PathVariable Long proposalId,
            Authentication authentication) {
        String email = authentication.getName();
        ProposalDTO proposalDTO = proposalService.rejectProposal(proposalId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Proposal rejected successfully", proposalDTO));
    }
}
