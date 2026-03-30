package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.DisputeDTO;
import com.exe202.skillnest.dto.MessageDTO;
import com.exe202.skillnest.entity.*;
import com.exe202.skillnest.enums.DisputeStatus;
import com.exe202.skillnest.enums.MessageType;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.mapper.DisputeMapper;
import com.exe202.skillnest.mapper.MessageMapper;
import com.exe202.skillnest.repository.*;
import com.exe202.skillnest.service.ManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerServiceImpl implements ManagerService {

    private final DisputeRepository disputeRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final DisputeMapper disputeMapper;
    private final MessageMapper messageMapper;
    private final com.exe202.skillnest.service.NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeDTO> getAllDisputes(DisputeStatus status, Pageable pageable) {
        if (status != null) {
            return disputeRepository.findByStatus(status, pageable)
                    .map(disputeMapper::toDTO);
        }
        return disputeRepository.findAll(pageable)
                .map(disputeMapper::toDTO);
    }

    @Override
    @Transactional
    public DisputeDTO updateDisputeStatus(Long disputeId, DisputeStatus newStatus, Long managerId) {
        log.info("Manager {} updating dispute {} status to {}", managerId, disputeId, newStatus);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("Dispute not found"));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("Manager not found"));

        // Validate role
        boolean hasPermission = manager.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("MANAGER"));

        if (!hasPermission) {
            throw new ForbiddenException("Only Admin or Manager can update dispute status");
        }

        dispute.setStatus(newStatus);
        dispute = disputeRepository.save(dispute);

        // Notify both parties about dispute status change
        Long clientId = dispute.getContract().getClient().getUserId();
        Long studentId = dispute.getContract().getStudent().getUserId();
        String statusMsg = "Dispute status updated to " + newStatus.name();
        notificationService.notify(clientId,
                com.exe202.skillnest.enums.NotificationType.DISPUTE_RESOLVED,
                "Dispute update", statusMsg, "DISPUTE", disputeId);
        notificationService.notify(studentId,
                com.exe202.skillnest.enums.NotificationType.DISPUTE_RESOLVED,
                "Dispute update", statusMsg, "DISPUTE", disputeId);

        log.info("Dispute status updated successfully: {}", disputeId);
        return disputeMapper.toDTO(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDTO> getDisputedContractMessages(Long disputeId, Pageable pageable) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("Dispute not found"));

        Contract contract = dispute.getContract();
        Conversation conversation = conversationRepository.findByContractContractId(contract.getContractId())
                .orElseThrow(() -> new NotFoundException("Conversation not found for this contract"));

        return messageRepository.findByConversationConversationIdOrderBySentAtDesc(
                conversation.getConversationId(), pageable)
                .map(messageMapper::toDTO);
    }

    @Override
    @Transactional
    public MessageDTO postSystemMessage(Long conversationId, String content, Long managerId) {
        log.info("Manager {} posting system message to conversation {}", managerId, conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("Manager not found"));

        // Validate role
        boolean hasPermission = manager.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("MANAGER"));

        if (!hasPermission) {
            throw new ForbiddenException("Only Admin or Manager can post system messages");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(manager)
                .content(content)
                .type(MessageType.SYSTEM)
                .sentAt(LocalDateTime.now())
                .build();

        message = messageRepository.save(message);

        log.info("System message posted successfully");
        return messageMapper.toDTO(message);
    }

    @Override
    @Transactional
    public void hideMessage(Long messageId, Long managerId) {
        log.info("Manager {} hiding message {}", managerId, messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("Manager not found"));

        // Validate role
        boolean hasPermission = manager.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("MANAGER"));

        if (!hasPermission) {
            throw new ForbiddenException("Only Admin or Manager can hide messages");
        }

        // Soft delete - replace content with [Hidden by moderator]
        message.setContent("[This message has been hidden by moderator]");
        messageRepository.save(message);

        log.info("Message hidden successfully: {}", messageId);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long adminId) {
        log.info("Admin {} permanently deleting message {}", adminId, messageId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        if (!isAdmin) {
            throw new ForbiddenException("Only Admin can permanently delete messages");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        messageRepository.delete(message);

        log.info("Message permanently deleted: {}", messageId);
    }
}
