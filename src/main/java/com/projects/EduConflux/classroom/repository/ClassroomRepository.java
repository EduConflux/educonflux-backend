package com.projects.EduConflux.classroom.repository;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.entity.ClassroomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository
        extends JpaRepository<Classroom, Long> {

    List<Classroom> findByInstitutionId(Long institutionId);

    List<Classroom> findByInstitutionIdAndStatus(
            Long institutionId,
            ClassroomStatus status
    );

    List<Classroom> findByFacultyIdAndInstitutionId(
            Long facultyId,
            Long institutionId
    );

    Optional<Classroom> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsByFacultyIdAndName(
            Long facultyId,
            String name
    );
}