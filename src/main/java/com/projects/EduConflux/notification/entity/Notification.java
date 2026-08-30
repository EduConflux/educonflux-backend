package com.projects.EduConflux.notification.entity;

import com.projects.EduConflux.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * User who receives this notification.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /*
     * Short notification heading.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /*
     * Notification message.
     */
    @Column(nullable = false, length = 1000)
    private String message;

    /*
     * Type of event that generated
     * the notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private NotificationType type;

    /*
     * ID of the related entity.
     *
     * Example:
     * invitation ID
     * assignment ID
     * timetable ID
     *
     * Can be null for general notifications.
     */
    @Column
    private Long referenceId;

    /*
     * Whether the user has opened/read
     * the notification.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    /*
     * When the notification was read.
     */
    @Column
    private LocalDateTime readAt;

    /*
     * When the notification was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (!read) {
            readAt = null;
        }
    }
}