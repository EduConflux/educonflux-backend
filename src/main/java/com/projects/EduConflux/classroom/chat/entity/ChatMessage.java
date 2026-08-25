package com.projects.EduConflux.classroom.chat.entity;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "classroom_id",
            nullable = false
    )
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "sender_id",
            nullable = false
    )
    private User sender;

    /*
     * NULL for classroom group chat.
     * Set for private faculty ↔ student chat.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "recipient_id"
    )
    private User recipient;

    @Column(
            nullable = false,
            length = 2000
    )
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "message_type",
            nullable = false,
            length = 20
    )
    private ChatMessageType messageType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();

        if (messageType == null) {
            messageType = ChatMessageType.TEXT;
        }
    }
}