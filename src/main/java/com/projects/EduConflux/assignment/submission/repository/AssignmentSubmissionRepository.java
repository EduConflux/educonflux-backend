package com.projects.EduConflux.assignment.submission.repository;

import com.projects.EduConflux.assignment.submission.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    Optional<AssignmentSubmission>
    findByAssignmentIdAndStudentId(
            Long assignmentId,
            Long studentId
    );

    List<AssignmentSubmission>
    findByAssignmentId(
            Long assignmentId
    );

    boolean existsByAssignmentIdAndStudentId(
            Long assignmentId,
            Long studentId
    );
}