package com.projects.EduConflux.classroom.membership.entity;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "classroom_memberships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_classroom_student",
                        columnNames = {
                                "classroom_id",
                                "student_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "classroom_id",
            nullable = false
    )
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column
    private LocalDateTime removedAt;

    @PrePersist
    protected void onCreate() {

        joinedAt = LocalDateTime.now();

        if (status == null) {
            status = MembershipStatus.ACTIVE;
        }
    }
}