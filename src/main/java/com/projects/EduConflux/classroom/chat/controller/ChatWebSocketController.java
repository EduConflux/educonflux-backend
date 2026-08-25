package com.projects.EduConflux.classroom.chat.controller;

import com.projects.EduConflux.classroom.chat.dto.ChatMessageRequest;
import com.projects.EduConflux.classroom.chat.dto.ChatMessageResponse;
import com.projects.EduConflux.classroom.chat.service.ChatMessageService;
import com.projects.EduConflux.common.config.JwtStompInterceptor;
import com.projects.EduConflux.common.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/classroom/{classroomId}/chat")
    @SendTo("/topic/classroom/{classroomId}/chat")
    public ChatMessageResponse sendMessage(
            @DestinationVariable Long classroomId,
            ChatMessageRequest request,
            Message<?> message) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        Map<String, Object> sessionAttributes =
                accessor.getSessionAttributes();

        if (sessionAttributes == null) {

            throw new IllegalStateException(
                    "WebSocket session attributes not available"
            );
        }

        Object userObject =
                sessionAttributes.get(
                        JwtStompInterceptor.AUTHENTICATED_USER
                );

        if (!(userObject instanceof AuthenticatedUser)) {

            throw new IllegalStateException(
                    "Authenticated WebSocket user not found in session"
            );
        }

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser) userObject;

        System.out.println(
                "WEBSOCKET SEND USER: "
                        + authenticatedUser.getEmail()
        );

        System.out.println(
                "WEBSOCKET SEND ROLES: "
                        + authenticatedUser.getRoles()
        );

        return chatMessageService.sendGroupMessage(
                classroomId,
                request.getContent(),
                authenticatedUser
        );
    }
}