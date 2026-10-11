package com.projects.EduConflux.parent.repository;

import com.projects.EduConflux.parent.entity.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentParentRepository
        extends JpaRepository<StudentParent, Long> {

    List<StudentParent> findByParentIdAndInstitutionId(
            Long parentId,
            Long institutionId
    );

    List<StudentParent> findByStudentIdAndInstitutionId(
            Long studentId,
            Long institutionId
    );

    Optional<StudentParent> findByParentIdAndStudentIdAndInstitutionId(
            Long parentId,
            Long studentId,
            Long institutionId
    );

    boolean existsByParentIdAndStudentIdAndInstitutionId(
            Long parentId,
            Long studentId,
            Long institutionId
    );
}
