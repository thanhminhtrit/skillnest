package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Notification;
import com.exe202.skillnest.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipient_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByRecipient_UserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    boolean existsByRecipient_UserIdAndTypeAndRelatedEntityTypeAndRelatedEntityId(
            Long recipientId, NotificationType type, String relatedEntityType, Long relatedEntityId);
}
