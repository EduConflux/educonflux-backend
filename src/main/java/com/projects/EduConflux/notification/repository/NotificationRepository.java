package com.projects.EduConflux.notification.repository;

import com.projects.EduConflux.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /*
     * Get all notifications for a user,
     * newest first.
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    /*
     * Get unread notifications for a user.
     */
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId
    );

    /*
     * Count unread notifications.
     */
    long countByUserIdAndReadFalse(
            Long userId
    );

    /*
     * Find a notification only if it belongs
     * to the authenticated user.
     *
     * This prevents one user from modifying
     * another user's notification.
     */
    Optional<Notification> findByIdAndUserId(
            Long notificationId,
            Long userId
    );
}