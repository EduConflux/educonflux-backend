package com.projects.EduConflux.assignment.submission.entity;

import com.projects.EduConflux.assignment.entity.Assignment;
import com.projects.EduConflux.files.entity.StoredFile;
import com.projects.EduConflux.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "assignment_submissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_assignment_student_submission",
                        columnNames = {
                                "assignment_id",
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
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Assignment being submitted.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "assignment_id",
            nullable = false
    )
    private Assignment assignment;


    /*
     * Student who submitted the assignment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;


    /*
     * File submitted by the student.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "file_id",
            nullable = false
    )
    private StoredFile file;


    /*
     * Time at which the assignment
     * was submitted.
     */
    @Column(nullable = false)
    private LocalDateTime submittedAt;


    /*
     * Marks given by faculty.
     *
     * Null until the submission
     * has been graded.
     */
    @Column
    private Integer marks;


    /*
     * Feedback given by faculty.
     *
     * Null until the submission
     * has been reviewed.
     */
    @Column(columnDefinition = "TEXT")
    private String feedback;


    /*
     * Time at which the submission
     * was graded.
     */
    @Column
    private LocalDateTime gradedAt;


    @PrePersist
    protected void onCreate() {

        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}