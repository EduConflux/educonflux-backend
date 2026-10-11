package com.projects.EduConflux.parent.repository;

import com.projects.EduConflux.parent.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    Optional<Parent> findByUserIdAndInstitutionId(
            Long userId,
            Long institutionId
    );

    List<Parent> findByInstitutionId(Long institutionId);

    boolean existsByUserId(Long userId);
}
