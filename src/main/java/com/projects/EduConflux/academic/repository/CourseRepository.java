package com.projects.EduConflux.academic.repository;

import com.projects.EduConflux.academic.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository
        extends JpaRepository<Course, Long> {

    List<Course> findByInstitutionId(Long institutionId);

    List<Course> findBySemesterIdAndInstitutionId(
            Long semesterId,
            Long institutionId
    );

    List<Course> findByProgramIdAndInstitutionId(
            Long programId,
            Long institutionId
    );

    Optional<Course> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByInstitutionIdAndCode(
            Long institutionId,
            String code
    );
}