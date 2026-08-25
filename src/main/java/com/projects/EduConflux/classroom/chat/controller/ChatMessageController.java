package com.projects.EduConflux.classroom.chat.controller;

import com.projects.EduConflux.classroom.chat.dto.ChatMessageResponse;
import com.projects.EduConflux.classroom.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{classroomId}/chat")
    public List<ChatMessageResponse> getChatHistory(
            @PathVariable Long classroomId,
            Principal principal) {

        return chatMessageService.getChatHistory(
                classroomId,
                principal
        );
    }
}