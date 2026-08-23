package com.projects.EduConflux.classroom.membership.repository;

import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomMembershipRepository
        extends JpaRepository<ClassroomMembership, Long> {

    List<ClassroomMembership> findByClassroomIdAndStatus(
            Long classroomId,
            MembershipStatus status
    );

    List<ClassroomMembership> findByStudentIdAndStatus(
            Long studentId,
            MembershipStatus status
    );

    Optional<ClassroomMembership> findByClassroomIdAndStudentId(
            Long classroomId,
            Long studentId
    );

    boolean existsByClassroomIdAndStudentId(
            Long classroomId,
            Long studentId
    );
}