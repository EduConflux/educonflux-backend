package com.projects.EduConflux.academic.repository;

import com.projects.EduConflux.academic.entity.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassSectionRepository
        extends JpaRepository<ClassSection, Long> {

    List<ClassSection> findByInstitutionId(
            Long institutionId
    );

    List<ClassSection> findBySemesterIdAndInstitutionId(
            Long semesterId,
            Long institutionId
    );

    Optional<ClassSection> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    boolean existsBySemesterIdAndName(
            Long semesterId,
            String name
    );
}