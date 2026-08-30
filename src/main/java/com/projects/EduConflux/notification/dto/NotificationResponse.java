package com.projects.EduConflux.notification.dto;

import com.projects.EduConflux.notification.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private Long userId;

    private String title;

    private String message;

    private NotificationType type;

    private Long referenceId;

    private boolean read;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}