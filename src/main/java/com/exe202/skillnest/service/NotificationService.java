package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.NotificationDTO;
import com.exe202.skillnest.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void notify(Long recipientId, NotificationType type,
                String title, String message,
                String relatedEntityType, Long relatedEntityId);
    Page<NotificationDTO> getMyNotifications(Long userId, Pageable pageable);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}
