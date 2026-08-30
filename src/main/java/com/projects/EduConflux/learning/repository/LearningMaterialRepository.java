package com.projects.EduConflux.learning.repository;

import com.projects.EduConflux.learning.entity.LearningMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningMaterialRepository
        extends JpaRepository<LearningMaterial, Long> {

    Page<LearningMaterial>
    findByClassroomId(
            Long classroomId,
            Pageable pageable
    );
}