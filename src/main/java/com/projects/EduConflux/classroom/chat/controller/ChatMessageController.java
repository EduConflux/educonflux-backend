package com.projects.EduConflux.classroom.chat.controller;

import com.projects.EduConflux.classroom.chat.dto.ChatMessageResponse;
import com.projects.EduConflux.classroom.chat.service.ChatMessageService;
import com.projects.EduConflux.common.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classrooms")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;


    /*
     * Get complete classroom group-chat history.
     *
     * GET /api/classrooms/{classroomId}/chat
     */
    @GetMapping("/{classroomId}/chat")
    public List<ChatMessageResponse> getChatHistory(
            @PathVariable Long classroomId,
            Principal principal) {

        return chatMessageService.getChatHistory(
                classroomId,
                principal
        );
    }


    /*
     * Get private conversation between
     * authenticated user and another user.
     *
     * GET /api/classrooms/{classroomId}/chat/private/{userId}
     */
    @GetMapping("/{classroomId}/chat/private/{userId}")
    public List<ChatMessageResponse> getPrivateConversation(
            @PathVariable Long classroomId,
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return chatMessageService.getPrivateConversation(
                classroomId,
                userId,
                authenticatedUser
        );
    }
}