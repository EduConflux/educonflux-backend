package com.projects.EduConflux.classroom.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(
            max = 2000,
            message = "Message cannot exceed 2000 characters"
    )
    private String content;
}