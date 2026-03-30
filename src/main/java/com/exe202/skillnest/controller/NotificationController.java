package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.NotificationDTO;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.NotificationService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification APIs")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;

    @GetMapping
    @Operation(summary = "Get my notifications (paginated)")
    public ResponseEntity<BaseResponse> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> notifications = notificationService.getMyNotifications(userId, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Notifications retrieved", notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count (for badge)")
    public ResponseEntity<BaseResponse> getUnreadCount() {
        Long userId = securityUtil.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(new BaseResponse(200, "Unread count", Map.of("unreadCount", count)));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<BaseResponse> markAsRead(@PathVariable Long notificationId) {
        Long userId = securityUtil.getCurrentUserId();
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(new BaseResponse(200, "Notification marked as read", null));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<BaseResponse> markAllAsRead() {
        Long userId = securityUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(new BaseResponse(200, "All notifications marked as read", null));
    }
}
