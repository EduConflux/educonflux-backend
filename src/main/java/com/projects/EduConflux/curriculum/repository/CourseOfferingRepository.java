package com.projects.EduConflux.curriculum.repository;

import com.projects.EduConflux.curriculum.entity.CourseOffering;
import com.projects.EduConflux.curriculum.entity.CourseOfferingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseOfferingRepository
        extends JpaRepository<CourseOffering, Long> {

    List<CourseOffering> findByInstitutionId(
            Long institutionId
    );

    Optional<CourseOffering> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByInstitutionIdAndCourseIdAndAcademicYearId(
            Long institutionId,
            Long courseId,
            Long academicYearId
    );

    List<CourseOffering> findByInstitutionIdAndAcademicYearId(
            Long institutionId,
            Long academicYearId
    );

    List<CourseOffering> findByInstitutionIdAndStatus(
            Long institutionId,
            CourseOfferingStatus status
    );
}