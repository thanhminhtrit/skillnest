package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.NotificationDTO;
import com.exe202.skillnest.entity.Notification;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.NotificationType;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.mapper.NotificationMapper;
import com.exe202.skillnest.repository.NotificationRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void notify(Long recipientId, NotificationType type,
                       String title, String message,
                       String relatedEntityType, Long relatedEntityId) {
        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            log.warn("Cannot send notification: user {} not found", recipientId);
            return;
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notification sent: type={} recipient={} entity={}:{}",
                type, recipientId, relatedEntityType, relatedEntityId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipient_UserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.getRecipient().getUserId().equals(userId)) {
            throw new ForbiddenException("You can only mark your own notifications as read");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsRead(userId);
        log.info("Marked {} notifications as read for user {}", updated, userId);
    }
}
