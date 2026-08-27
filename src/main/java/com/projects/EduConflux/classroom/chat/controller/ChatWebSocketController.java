package com.projects.EduConflux.classroom.chat.controller;

import com.projects.EduConflux.classroom.chat.dto.ChatMessageRequest;
import com.projects.EduConflux.classroom.chat.dto.ChatMessageResponse;
import com.projects.EduConflux.classroom.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;


    /*
     * Classroom group chat
     *
     * Client sends:
     *
     * /app/classroom/{classroomId}/chat
     *
     * Server broadcasts:
     *
     * /topic/classroom/{classroomId}/chat
     */
    @MessageMapping("/classroom/{classroomId}/chat")
    @SendTo("/topic/classroom/{classroomId}/chat")
    public ChatMessageResponse sendMessage(
            @DestinationVariable Long classroomId,
            ChatMessageRequest request,
            Principal principal) {

        return chatMessageService.sendGroupMessage(
                classroomId,
                request.getContent(),
                principal
        );
    }


    /*
     * Private faculty <-> student chat
     *
     * Client sends:
     *
     * /app/classroom/{classroomId}/chat/private/{recipientId}
     *
     * Server broadcasts:
     *
     * /topic/classroom/{classroomId}/chat/private/{recipientId}
     */
    @MessageMapping("/classroom/{classroomId}/chat/private/{recipientId}")
    @SendTo("/topic/classroom/{classroomId}/chat/private/{recipientId}")
    public ChatMessageResponse sendPrivateMessage(
            @DestinationVariable Long classroomId,
            @DestinationVariable Long recipientId,
            ChatMessageRequest request,
            Principal principal) {

        return chatMessageService.sendPrivateMessage(
                classroomId,
                recipientId,
                request.getContent(),
                principal
        );
    }
}