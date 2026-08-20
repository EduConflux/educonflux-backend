package com.projects.EduConflux.academic.repository;

import com.projects.EduConflux.academic.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository
        extends JpaRepository<Program, Long> {

    List<Program> findByInstitutionId(Long institutionId);

    Optional<Program> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByDepartmentIdAndName(
            Long departmentId,
            String name
    );

    boolean existsByDepartmentIdAndCode(
            Long departmentId,
            String code
    );
}