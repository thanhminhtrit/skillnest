package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.ConversationDTO;
import com.exe202.skillnest.dto.MessageDTO;
import com.exe202.skillnest.payloads.request.SendMessageRequest;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.ConversationService;
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
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation", description = "Conversation and Message APIs")
@SecurityRequirement(name = "bearer-jwt")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/contract/{contractId}")
    @Operation(summary = "Create conversation for contract (participants only)")
    public ResponseEntity<BaseResponse> createConversation(
            @PathVariable Long contractId,
            Authentication authentication) {
        String email = authentication.getName();
        ConversationDTO conversationDTO = conversationService.createConversation(contractId, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Conversation created successfully", conversationDTO));
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get conversation by contract ID (participants only)")
    public ResponseEntity<BaseResponse> getConversationByContractId(
            @PathVariable Long contractId,
            Authentication authentication) {
        String email = authentication.getName();
        ConversationDTO conversationDTO = conversationService.getConversationByContractId(contractId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Conversation retrieved successfully", conversationDTO));
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Send message (participants only)")
    public ResponseEntity<BaseResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        MessageDTO messageDTO = conversationService.sendMessage(conversationId, request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Message sent successfully", messageDTO));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get messages in conversation (participants only)")
    public ResponseEntity<BaseResponse> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<MessageDTO> messages = conversationService.getMessages(conversationId, email, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Messages retrieved successfully", messages));
    }
}

