package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.ContractDTO;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract", description = "Contract APIs")
@SecurityRequirement(name = "bearer-jwt")
public class ContractController {

    private final ContractService contractService;

    @PostMapping("/proposal/{proposalId}")
    @Operation(summary = "Create contract from accepted proposal (project owner only)")
    public ResponseEntity<BaseResponse> createContract(
            @PathVariable Long proposalId,
            Authentication authentication) {
        String email = authentication.getName();
        ContractDTO contractDTO = contractService.createContract(proposalId, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Contract created successfully", contractDTO));
    }

    @PostMapping("/{contractId}/activate")
    @Operation(summary = "Activate contract (participants only)")
    public ResponseEntity<BaseResponse> activateContract(
            @PathVariable Long contractId,
            Authentication authentication) {
        String email = authentication.getName();
        ContractDTO contractDTO = contractService.activateContract(contractId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Contract activated successfully", contractDTO));
    }

    @PostMapping("/{contractId}/complete")
    @Operation(summary = "Complete contract (client only)")
    public ResponseEntity<BaseResponse> completeContract(
            @PathVariable Long contractId,
            Authentication authentication) {
        String email = authentication.getName();
        ContractDTO contractDTO = contractService.completeContract(contractId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Contract completed successfully", contractDTO));
    }

    @PostMapping("/{contractId}/cancel")
    @Operation(summary = "Cancel contract (participants only)")
    public ResponseEntity<BaseResponse> cancelContract(
            @PathVariable Long contractId,
            Authentication authentication) {
        String email = authentication.getName();
        ContractDTO contractDTO = contractService.cancelContract(contractId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Contract cancelled successfully", contractDTO));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my contracts")
    public ResponseEntity<BaseResponse> getMyContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ContractDTO> contracts = contractService.getMyContracts(email, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Contracts retrieved successfully", contracts));
    }

    @GetMapping("/{contractId}")
    @Operation(summary = "Get contract by ID (participants only)")
    public ResponseEntity<BaseResponse> getContractById(
            @PathVariable Long contractId,
            Authentication authentication) {
        String email = authentication.getName();
        ContractDTO contractDTO = contractService.getContractById(contractId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Contract retrieved successfully", contractDTO));
    }
}

