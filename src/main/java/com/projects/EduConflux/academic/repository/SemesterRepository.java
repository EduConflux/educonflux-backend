package com.projects.EduConflux.academic.repository;

import com.projects.EduConflux.academic.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SemesterRepository
        extends JpaRepository<Semester, Long> {

    List<Semester> findByInstitutionId(Long institutionId);

    List<Semester> findByProgramIdAndInstitutionId(
            Long programId,
            Long institutionId
    );

    Optional<Semester> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByProgramIdAndSemesterNumber(
            Long programId,
            Integer semesterNumber
    );
}