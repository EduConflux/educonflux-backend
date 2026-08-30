package com.projects.EduConflux.notification.service;

import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.notification.dto.NotificationRequest;
import com.projects.EduConflux.notification.dto.NotificationResponse;
import com.projects.EduConflux.notification.entity.Notification;
import com.projects.EduConflux.notification.entity.NotificationType;
import com.projects.EduConflux.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;


    /*
     * Create a notification.
     */
    @Transactional
    public NotificationResponse createNotification(
            NotificationRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        User user =
                userRepository
                        .findByIdAndInstitutionId(
                                request.getUserId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));


        Notification notification =
                Notification.builder()
                        .user(user)
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .type(request.getType())
                        .referenceId(request.getReferenceId())
                        .read(false)
                        .build();


        Notification saved =
                notificationRepository.save(
                        notification
                );


        return toResponse(saved);
    }


    /*
     * Get all notifications for the
     * currently authenticated user.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getMyNotifications() {

        Long userId =
                SecurityUtils.getCurrentUserId();


        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    /*
     * Get only unread notifications.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getUnreadNotifications() {

        Long userId =
                SecurityUtils.getCurrentUserId();


        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    /*
     * Get unread notification count.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {

        Long userId =
                SecurityUtils.getCurrentUserId();


        return notificationRepository
                .countByUserIdAndReadFalse(userId);
    }


    /*
     * Mark one notification as read.
     */
    @Transactional
    public NotificationResponse markAsRead(
            Long notificationId) {

        Long userId =
                SecurityUtils.getCurrentUserId();


        Notification notification =
                notificationRepository
                        .findByIdAndUserId(
                                notificationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                ));


        if (!notification.isRead()) {

            notification.setRead(true);

            notification.setReadAt(
                    LocalDateTime.now()
            );

            notificationRepository.save(
                    notification
            );
        }


        return toResponse(notification);
    }


    /*
     * Mark all notifications as read.
     */
    @Transactional
    public void markAllAsRead() {

        Long userId =
                SecurityUtils.getCurrentUserId();


        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                userId
                        );


        LocalDateTime now =
                LocalDateTime.now();


        for (Notification notification :
                notifications) {

            notification.setRead(true);
            notification.setReadAt(now);
        }


        notificationRepository.saveAll(
                notifications
        );
    }


    /*
     * Convert entity to response DTO.
     */
    private NotificationResponse toResponse(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(
                        notification.getUser().getId()
                )
                .title(
                        notification.getTitle()
                )
                .message(
                        notification.getMessage()
                )
                .type(
                        notification.getType()
                )
                .referenceId(
                        notification.getReferenceId()
                )
                .read(
                        notification.isRead()
                )
                .readAt(
                        notification.getReadAt()
                )
                .createdAt(
                        notification.getCreatedAt()
                )
                .build();
    }

    @Transactional
    public NotificationResponse createNotification(
            User user,
            String title,
            String message,
            NotificationType type,
            Long referenceId) {

        Notification notification =
                Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .type(type)
                        .referenceId(referenceId)
                        .read(false)
                        .build();

        Notification saved =
                notificationRepository.save(notification);

        return toResponse(saved);
    }
}