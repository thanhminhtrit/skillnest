package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.NotificationDTO;
import com.exe202.skillnest.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationDTO toDTO(Notification entity) {
        if (entity == null) return null;
        return NotificationDTO.builder()
                .notificationId(entity.getNotificationId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .relatedEntityType(entity.getRelatedEntityType())
                .relatedEntityId(entity.getRelatedEntityId())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
