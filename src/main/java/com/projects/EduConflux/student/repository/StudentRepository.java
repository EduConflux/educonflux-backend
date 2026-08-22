package com.projects.EduConflux.student.repository;

import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    boolean existsByInstitutionIdAndEnrollmentNumber(
            Long institutionId,
            String enrollmentNumber
    );

    boolean existsByInstitutionIdAndEmail(
            Long institutionId,
            String email
    );

    Optional<Student> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    List<Student> findByInstitutionId(
            Long institutionId
    );

    List<Student> findByInstitutionIdAndStatus(
            Long institutionId,
            StudentStatus status
    );

    List<Student> findByInstitutionIdAndProgramId(
            Long institutionId,
            Long programId
    );

    List<Student> findByInstitutionIdAndClassSectionId(
            Long institutionId,
            Long classSectionId
    );

    Optional<Student> findByInstitutionIdAndEmail(
            Long institutionId,
            String email
    );
}