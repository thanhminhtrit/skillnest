package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.DisputeDTO;
import com.exe202.skillnest.dto.MessageDTO;
import com.exe202.skillnest.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ManagerService {

    /**
     * Get all disputes with filters (Admin/Manager)
     */
    Page<DisputeDTO> getAllDisputes(DisputeStatus status, Pageable pageable);

    /**
     * Update dispute status (Admin/Manager)
     */
    DisputeDTO updateDisputeStatus(Long disputeId, DisputeStatus newStatus, Long managerId);

    /**
     * Get all messages in a disputed contract (Admin/Manager)
     */
    Page<MessageDTO> getDisputedContractMessages(Long disputeId, Pageable pageable);

    /**
     * Post system message to conversation (Admin/Manager)
     */
    MessageDTO postSystemMessage(Long conversationId, String content, Long managerId);

    /**
     * Hide message (soft delete for moderation) - Admin/Manager
     */
    void hideMessage(Long messageId, Long managerId);

    /**
     * Permanently delete message (Admin only)
     */
    void deleteMessage(Long messageId, Long adminId);
}

