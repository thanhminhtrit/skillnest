package com.exe202.skillnest.controller;

import com.exe202.skillnest.config.security.IsAdmin;
import com.exe202.skillnest.config.security.IsManager;
import com.exe202.skillnest.dto.DisputeDTO;
import com.exe202.skillnest.dto.MessageDTO;
import com.exe202.skillnest.enums.DisputeStatus;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.ManagerService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Tag(name = "Manager Operations", description = "Dispute handling and message moderation")
@SecurityRequirement(name = "bearer-jwt")
public class ManagerController {

    private final ManagerService managerService;
    private final SecurityUtil securityUtil;

    @GetMapping("/disputes")
    @IsManager
    @Operation(summary = "Get all disputes with filters (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> getAllDisputes(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DisputeDTO> disputes = managerService.getAllDisputes(status, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Disputes retrieved successfully", disputes));
    }

    @PutMapping("/disputes/{disputeId}/status")
    @IsManager
    @Operation(summary = "Update dispute status (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> updateDisputeStatus(
            @PathVariable Long disputeId,
            @RequestParam DisputeStatus status) {
        Long managerId = securityUtil.getCurrentUserId();
        DisputeDTO dispute = managerService.updateDisputeStatus(disputeId, status, managerId);
        return ResponseEntity.ok(new BaseResponse(200, "Dispute status updated successfully", dispute));
    }

    @GetMapping("/disputes/{disputeId}/messages")
    @IsManager
    @Operation(summary = "View all messages in disputed contract (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> getDisputedContractMessages(
            @PathVariable Long disputeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<MessageDTO> messages = managerService.getDisputedContractMessages(disputeId, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Messages retrieved successfully", messages));
    }

    @PostMapping("/messages/system")
    @IsManager
    @Operation(summary = "Post system message to conversation (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> postSystemMessage(
            @RequestParam Long conversationId,
            @RequestParam String content) {
        Long managerId = securityUtil.getCurrentUserId();
        MessageDTO message = managerService.postSystemMessage(conversationId, content, managerId);
        return ResponseEntity.ok(new BaseResponse(200, "System message posted successfully", message));
    }

    @PutMapping("/messages/{messageId}/hide")
    @IsManager
    @Operation(summary = "Hide message for moderation (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> hideMessage(@PathVariable Long messageId) {
        Long managerId = securityUtil.getCurrentUserId();
        managerService.hideMessage(messageId, managerId);
        return ResponseEntity.ok(new BaseResponse(200, "Message hidden successfully", null));
    }

    @DeleteMapping("/messages/{messageId}")
    @IsAdmin
    @Operation(summary = "Permanently delete message (ADMIN only)")
    public ResponseEntity<BaseResponse> deleteMessage(@PathVariable Long messageId) {
        Long adminId = securityUtil.getCurrentUserId();
        managerService.deleteMessage(messageId, adminId);
        return ResponseEntity.ok(new BaseResponse(200, "Message deleted permanently", null));
    }
}
