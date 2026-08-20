package com.projects.EduConflux.identity.repository;

import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndInstitutionId(
            Long userId,
            Long institutionId
    );

    List<User> findAllByInstitutionId(
            Long institutionId
    );

    @Query("""
        SELECT DISTINCT ur.user
        FROM UserRole ur
        WHERE ur.user.institution.id = :institutionId
        AND ur.role.name = :roleName
        """)
    List<User> findAllByInstitutionIdAndRoleName(
            @Param("institutionId") Long institutionId,
            @Param("roleName") String roleName
    );

    List<User> findAllByInstitutionIdAndStatus(
            Long institutionId,
            UserStatus status
    );

    @Query("""
        SELECT DISTINCT u
        FROM User u
        WHERE u.institution.id = :institutionId

        AND (
            :role IS NULL
            OR :role = ''
            OR EXISTS (
                SELECT ur.id
                FROM UserRole ur
                WHERE ur.user = u
                AND ur.role.name = :role
            )
        )

        AND (
            :status IS NULL
            OR u.status = :status
        )

        AND (
            :search IS NULL
            OR :search = ''
            OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(u.personalEmail) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<User> findUsersWithFilters(
            @Param("institutionId") Long institutionId,
            @Param("role") String role,
            @Param("status") UserStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}