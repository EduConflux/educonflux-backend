package com.projects.EduConflux.academic.repository;

import com.projects.EduConflux.academic.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department> findByInstitutionId(Long institutionId);

    Optional<Department> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByInstitutionIdAndName(
            Long institutionId,
            String name
    );

    boolean existsByInstitutionIdAndCode(
            Long institutionId,
            String code
    );
}