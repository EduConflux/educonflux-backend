package com.projects.EduConflux.classroom.post.repository;

import com.projects.EduConflux.classroom.post.entity.ClassroomPost;
import com.projects.EduConflux.classroom.post.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassroomPostRepository
        extends JpaRepository<ClassroomPost, Long> {

    Page<ClassroomPost> findByClassroomId(
            Long classroomId,
            Pageable pageable
    );

    Page<ClassroomPost> findByClassroomIdAndType(
            Long classroomId,
            PostType type,
            Pageable pageable
    );

    Optional<ClassroomPost> findByIdAndClassroomId(
            Long id,
            Long classroomId
    );
}