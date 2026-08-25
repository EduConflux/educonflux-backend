package com.projects.EduConflux.classroom.chat.dto;

import com.projects.EduConflux.classroom.chat.entity.ChatMessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    private Long id;

    private Long classroomId;

    private Long senderId;

    private String senderName;

    private String content;

    private ChatMessageType messageType;

    private LocalDateTime createdAt;
}