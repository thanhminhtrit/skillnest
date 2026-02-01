package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.ConversationDTO;
import com.exe202.skillnest.dto.MessageDTO;
import com.exe202.skillnest.payloads.request.SendMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConversationService {
    ConversationDTO createConversation(Long contractId, String email);
    MessageDTO sendMessage(Long conversationId, SendMessageRequest request, String email);
    Page<MessageDTO> getMessages(Long conversationId, String email, Pageable pageable);
    ConversationDTO getConversationByContractId(Long contractId, String email);
}
