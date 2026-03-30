package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.ConversationDTO;
import com.exe202.skillnest.dto.MessageDTO;
import com.exe202.skillnest.entity.Contract;
import com.exe202.skillnest.entity.Conversation;
import com.exe202.skillnest.entity.Message;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.MessageType;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.payloads.request.SendMessageRequest;
import com.exe202.skillnest.repository.ContractRepository;
import com.exe202.skillnest.repository.ConversationRepository;
import com.exe202.skillnest.repository.MessageRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final com.exe202.skillnest.service.NotificationService notificationService;

    @Override
    @Transactional
    public ConversationDTO createConversation(Long contractId, String email) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user is participant
        if (!isParticipant(contract, user.getUserId())) {
            throw new ForbiddenException("You can only create conversation for contracts you are part of");
        }

        // Check if conversation already exists
        if (conversationRepository.findByContractContractId(contractId).isPresent()) {
            throw new ForbiddenException("Conversation already exists for this contract");
        }

        Conversation conversation = Conversation.builder()
                .contract(contract)
                .build();

        conversation = conversationRepository.save(conversation);
        return convertToDTO(conversation);
    }

    @Override
    @Transactional
    public MessageDTO sendMessage(Long conversationId, SendMessageRequest request, String email) {
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Conversation conversation = conversationRepository.findByConversationIdAndParticipant(conversationId, sender.getUserId())
                .orElseThrow(() -> new NotFoundException("Conversation not found or you don't have access"));

        // Parse message type with validation
        MessageType messageType;
        try {
            String typeStr = request.getType();
            if (typeStr == null || typeStr.trim().isEmpty()) {
                messageType = MessageType.TEXT; // default
            } else {
                messageType = MessageType.valueOf(typeStr.trim().toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid message type. Allowed values: TEXT, FILE, SYSTEM");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .type(messageType)
                .content(request.getContent())
                .fileUrl(request.getFileUrl())
                .build();

        message = messageRepository.save(message);

        // Notify the other party about new message
        Long recipientId = conversation.getContract().getClient().getUserId().equals(sender.getUserId())
                ? conversation.getContract().getStudent().getUserId()
                : conversation.getContract().getClient().getUserId();
        notificationService.notify(
                recipientId,
                com.exe202.skillnest.enums.NotificationType.NEW_MESSAGE,
                "New message",
                sender.getFullName() + " sent you a message",
                "CONVERSATION",
                conversationId
        );

        return convertToMessageDTO(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessages(Long conversationId, String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user has access to this conversation
        conversationRepository.findByConversationIdAndParticipant(conversationId, user.getUserId())
                .orElseThrow(() -> new NotFoundException("Conversation not found or you don't have access"));

        return messageRepository.findByConversationConversationIdOrderBySentAtDesc(conversationId, pageable)
                .map(this::convertToMessageDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDTO getConversationByContractId(Long contractId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Contract contract = contractRepository.findByContractIdAndParticipant(contractId, user.getUserId())
                .orElseThrow(() -> new NotFoundException("Contract not found or you don't have access"));

        Conversation conversation = conversationRepository.findByContractContractId(contractId)
                .orElseThrow(() -> new NotFoundException("Conversation not found for this contract"));

        return convertToDTO(conversation);
    }

    private boolean isParticipant(Contract contract, Long userId) {
        return contract.getClient().getUserId().equals(userId) ||
               contract.getStudent().getUserId().equals(userId);
    }

    private ConversationDTO convertToDTO(Conversation conversation) {
        return ConversationDTO.builder()
                .conversationId(conversation.getConversationId())
                .contractId(conversation.getContract().getContractId())
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    private MessageDTO convertToMessageDTO(Message message) {
        return MessageDTO.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation().getConversationId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getFullName())
                .type(message.getType())
                .content(message.getContent())
                .fileUrl(message.getFileUrl())
                .sentAt(message.getSentAt())
                .build();
    }
}
