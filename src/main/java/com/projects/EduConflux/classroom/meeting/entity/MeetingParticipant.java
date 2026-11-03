package com.projects.EduConflux.classroom.meeting.entity;

import com.projects.EduConflux.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "meeting_participants",
        indexes = {
                @Index(name = "idx_mp_meeting_id", columnList = "meeting_id"),
                @Index(name = "idx_mp_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private ClassroomMeeting meeting;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantRole role;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    private Integer durationSeconds;

    @Builder.Default
    @Column(nullable = false)
    private boolean handRaised = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean micMuted = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean cameraOff = false;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (role == null) {
            role = ParticipantRole.PARTICIPANT;
        }
    }
}

