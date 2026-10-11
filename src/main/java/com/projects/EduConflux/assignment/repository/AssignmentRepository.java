package com.projects.EduConflux.assignment.repository;

import com.projects.EduConflux.assignment.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    Page<Assignment> findByClassroomId(
            Long classroomId,
            Pageable pageable
    );

    Page<Assignment> findByClassroomIdIn(
            Collection<Long> classroomIds,
            Pageable pageable
    );
}
