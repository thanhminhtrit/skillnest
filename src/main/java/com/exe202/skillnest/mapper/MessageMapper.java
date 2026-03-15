package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.MessageDTO;
import com.exe202.skillnest.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageDTO toDTO(Message entity) {
        if (entity == null) {
            return null;
        }

        return MessageDTO.builder()
                .messageId(entity.getMessageId())
                .conversationId(entity.getConversation() != null ? entity.getConversation().getConversationId() : null)
                .senderId(entity.getSender() != null ? entity.getSender().getUserId() : null)
                .senderName(entity.getSender() != null ? entity.getSender().getFullName() : null)
                .content(entity.getContent())
                .type(entity.getType())
                .fileUrl(entity.getFileUrl())
                .sentAt(entity.getSentAt())
                .build();
    }
}

