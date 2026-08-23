package com.projects.EduConflux.classroom.invitation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {

    private Long id;

    private Long classroomId;

    private String classroomName;

    private String token;

    private String invitationLink;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private boolean active;
}