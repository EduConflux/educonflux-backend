package com.projects.EduConflux.faculty.repository;

import com.projects.EduConflux.faculty.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyRepository
        extends JpaRepository<Faculty, Long> {

    List<Faculty> findByInstitutionId(Long institutionId);

    List<Faculty> findByDepartmentIdAndInstitutionId(
            Long departmentId,
            Long institutionId
    );

    Optional<Faculty> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByInstitutionIdAndEmployeeId(
            Long institutionId,
            String employeeId
    );

    Optional<Faculty> findByInstitutionIdAndEmail(
            Long institutionId,
            String email
    );
}