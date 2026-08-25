package com.projects.EduConflux.classroom.chat.repository;

import com.projects.EduConflux.classroom.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByClassroomIdOrderByCreatedAtAsc(
            Long classroomId
    );

    List<ChatMessage> findByClassroomIdAndRecipientIsNullOrderByCreatedAtAsc(
            Long classroomId
    );
}