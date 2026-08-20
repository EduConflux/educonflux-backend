package com.projects.EduConflux.academic.repository;

import com.projects.EduConflux.academic.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicYearRepository
        extends JpaRepository<AcademicYear, Long> {

    List<AcademicYear> findByInstitutionId(Long institutionId);

    Optional<AcademicYear> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByInstitutionIdAndName(
            Long institutionId,
            String name
    );
}