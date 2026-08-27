package com.projects.EduConflux.classroom.chat.repository;

import com.projects.EduConflux.classroom.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    /*
     * All classroom messages.
     */
    List<ChatMessage> findByClassroomIdOrderByCreatedAtAsc(
            Long classroomId
    );


    /*
     * Classroom-wide messages only.
     *
     * recipient = null
     */
    List<ChatMessage>
    findByClassroomIdAndRecipientIsNullOrderByCreatedAtAsc(
            Long classroomId
    );


    /*
     * Private conversation between two users
     * inside a classroom.
     *
     * A -> B
     * OR
     * B -> A
     */
    @Query("""
        SELECT m
        FROM ChatMessage m
        WHERE m.classroom.id = :classroomId
        AND (
            (m.sender.id = :user1Id
             AND m.recipient.id = :user2Id)
            OR
            (m.sender.id = :user2Id
             AND m.recipient.id = :user1Id)
        )
        ORDER BY m.createdAt ASC
        """)
    List<ChatMessage> findPrivateConversation(
            @Param("classroomId") Long classroomId,
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );
}