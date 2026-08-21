package com.projects.EduConflux.student.entity;

import com.projects.EduConflux.academic.entity.AcademicYear;
import com.projects.EduConflux.academic.entity.ClassSection;
import com.projects.EduConflux.academic.entity.Program;
import com.projects.EduConflux.institution.entity.Institution;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_institution_enrollment",
                        columnNames = {
                                "institution_id",
                                "enrollment_number"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_student_institution_email",
                        columnNames = {
                                "institution_id",
                                "email"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "institution_id",
            nullable = false
    )
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "program_id",
            nullable = false
    )
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "class_section_id"
    )
    private ClassSection classSection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "academic_year_id",
            nullable = false
    )
    private AcademicYear academicYear;

    @Column(
            name = "enrollment_number",
            nullable = false,
            length = 50
    )
    private String enrollmentNumber;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    // Official EduConflux login email
    @Column(nullable = false, length = 150)
    private String email;

    // Personal email for credentials/recovery
    @Column(nullable = false, length = 150)
    private String personalEmail;

    @Column(length = 20)
    private String phone;

    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status;

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
            status = StudentStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}