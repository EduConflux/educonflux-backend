package com.projects.EduConflux.curriculum.repository;

import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignment;
import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyCourseAssignmentRepository
        extends JpaRepository<FacultyCourseAssignment, Long> {

    List<FacultyCourseAssignment>
    findByFacultyInstitutionId(Long institutionId);

    List<FacultyCourseAssignment>
    findByCourseOfferingInstitutionId(Long institutionId);

    Optional<FacultyCourseAssignment>
    findByIdAndFacultyInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByFacultyIdAndCourseOfferingId(
            Long facultyId,
            Long courseOfferingId
    );

    List<FacultyCourseAssignment>
    findByFacultyIdAndStatus(
            Long facultyId,
            FacultyCourseAssignmentStatus status
    );

    List<FacultyCourseAssignment>
    findByCourseOfferingIdAndStatus(
            Long courseOfferingId,
            FacultyCourseAssignmentStatus status
    );
}