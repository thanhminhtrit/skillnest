package com.exe202.skillnest.dto;

import com.exe202.skillnest.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private MessageType type;
    private String fileUrl;
    private LocalDateTime sentAt;
}
