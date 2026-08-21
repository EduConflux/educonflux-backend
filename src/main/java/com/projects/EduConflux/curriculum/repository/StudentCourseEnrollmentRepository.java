package com.projects.EduConflux.curriculum.repository;

import com.projects.EduConflux.curriculum.entity.StudentCourseEnrollment;
import com.projects.EduConflux.curriculum.entity.StudentCourseEnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentCourseEnrollmentRepository
        extends JpaRepository<StudentCourseEnrollment, Long> {

    List<StudentCourseEnrollment>
    findByStudentInstitutionId(Long institutionId);

    Optional<StudentCourseEnrollment>
    findByIdAndStudentInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByStudentIdAndCourseOfferingId(
            Long studentId,
            Long courseOfferingId
    );

    List<StudentCourseEnrollment>
    findByStudentIdAndStatus(
            Long studentId,
            StudentCourseEnrollmentStatus status
    );

    List<StudentCourseEnrollment>
    findByCourseOfferingIdAndStatus(
            Long courseOfferingId,
            StudentCourseEnrollmentStatus status
    );
}