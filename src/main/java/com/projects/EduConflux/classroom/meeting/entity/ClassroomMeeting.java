package com.projects.EduConflux.classroom.meeting.entity;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "classroom_meetings",
        indexes = {
                @Index(name = "idx_meeting_classroom", columnList = "classroom_id"),
                @Index(name = "idx_meeting_status", columnList = "status"),
                @Index(name = "idx_meeting_room_code", columnList = "room_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeetingStatus status;

    @Column(name = "room_code", nullable = false, unique = true, length = 64)
    private String roomCode;

    private LocalDateTime scheduledStartTime;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean allowStudentUnmute = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean allowStudentScreenShare = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = MeetingStatus.ACTIVE;
        }
        if (startedAt == null && status == MeetingStatus.ACTIVE) {
            startedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

