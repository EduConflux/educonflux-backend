package com.projects.EduConflux.classroom.invitation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinClassroomRequest {

    @NotBlank(message = "Invitation token is required")
    private String token;
}