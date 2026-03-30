package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.ConversationDTO;
import com.exe202.skillnest.entity.Conversation;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {
    public ConversationDTO toDTO(Conversation entity) {
        if (entity == null) return null;
        return ConversationDTO.builder()
                .conversationId(entity.getConversationId())
                .contractId(entity.getContract().getContractId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
