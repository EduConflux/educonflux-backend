package com.projects.EduConflux.notification.controller;

import com.projects.EduConflux.notification.dto.NotificationRequest;
import com.projects.EduConflux.notification.dto.NotificationResponse;
import com.projects.EduConflux.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;


    /*
     * Create notification.
     *
     * Mainly intended for internal/admin/event
     * driven usage.
     */
    @PostMapping
    public ResponseEntity<NotificationResponse>
    createNotification(
            @Valid @RequestBody NotificationRequest request) {

        return ResponseEntity.ok(
                notificationService
                        .createNotification(request)
        );
    }


    /*
     * Get all notifications for
     * authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getMyNotifications() {

        return ResponseEntity.ok(
                notificationService
                        .getMyNotifications()
        );
    }


    /*
     * Get unread notifications.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>>
    getUnreadNotifications() {

        return ResponseEntity.ok(
                notificationService
                        .getUnreadNotifications()
        );
    }


    /*
     * Get unread count.
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long>
    getUnreadCount() {

        return ResponseEntity.ok(
                notificationService
                        .getUnreadCount()
        );
    }


    /*
     * Mark one notification as read.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse>
    markAsRead(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService
                        .markAsRead(notificationId)
        );
    }


    /*
     * Mark all notifications as read.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void>
    markAllAsRead() {

        notificationService.markAllAsRead();

        return ResponseEntity.noContent()
                .build();
    }
}