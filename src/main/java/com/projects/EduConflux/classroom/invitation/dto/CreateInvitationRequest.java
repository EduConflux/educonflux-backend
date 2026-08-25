package com.projects.EduConflux.classroom.invitation.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInvitationRequest {

    @NotEmpty(message = "At least one student must be selected")
    private List<Long> studentIds;
}