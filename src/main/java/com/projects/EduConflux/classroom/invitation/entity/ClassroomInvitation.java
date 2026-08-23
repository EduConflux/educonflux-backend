package com.projects.EduConflux.classroom.invitation.entity;

import com.projects.EduConflux.classroom.entity.Classroom;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "classroom_invitations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_classroom_invitation_token",
                        columnNames = "token"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "classroom_id",
            nullable = false
    )
    private Classroom classroom;

    @Column(
            nullable = false,
            unique = true,
            length = 128
    )
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}