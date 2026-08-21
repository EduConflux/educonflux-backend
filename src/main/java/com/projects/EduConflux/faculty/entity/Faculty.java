package com.projects.EduConflux.faculty.entity;

import com.projects.EduConflux.academic.entity.Department;
import com.projects.EduConflux.institution.entity.Institution;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "faculty",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_faculty_institution_employee_id",
                        columnNames = {"institution_id", "employee_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty {

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
            name = "department_id",
            nullable = false
    )
    private Department department;

    @Column(
            name = "employee_id",
            nullable = false,
            length = 50
    )
    private String employeeId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(
            nullable = false,
            unique = false,
            length = 150
    )
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String designation;

    @Column(length = 255)
    private String qualification;

    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FacultyStatus status;

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
            status = FacultyStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}