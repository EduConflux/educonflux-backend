package com.projects.EduConflux.timetable.entity;

import com.projects.EduConflux.academic.entity.ClassSection;
import com.projects.EduConflux.curriculum.entity.CourseOffering;
import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignment;
import com.projects.EduConflux.institution.entity.Institution;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "timetable_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_timetable_section_day_time",
                        columnNames = {
                                "class_section_id",
                                "day_of_week",
                                "start_time"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableEntry {

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
            name = "class_section_id",
            nullable = false
    )
    private ClassSection classSection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "course_offering_id",
            nullable = false
    )
    private CourseOffering courseOffering;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "faculty_assignment_id",
            nullable = false
    )
    private FacultyCourseAssignment facultyAssignment;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "day_of_week",
            nullable = false,
            length = 20
    )
    private DayOfWeek dayOfWeek;

    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalTime startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalTime endTime;

    @Column(
            name = "room",
            length = 100
    )
    private String room;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private TimetableStatus status;

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
            status = TimetableStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}