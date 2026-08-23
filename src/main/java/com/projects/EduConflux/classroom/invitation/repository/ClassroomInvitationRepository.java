package com.projects.EduConflux.classroom.invitation.repository;

import com.projects.EduConflux.classroom.invitation.entity.ClassroomInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomInvitationRepository
        extends JpaRepository<ClassroomInvitation, Long> {

    Optional<ClassroomInvitation> findByTokenAndActiveTrue(
            String token
    );

    List<ClassroomInvitation> findByClassroomId(
            Long classroomId
    );
}