package com.projects.EduConflux.assignment.submission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubmissionRequest {

    @NotNull(message = "Assignment is required")
    private Long assignmentId;

    @NotNull(message = "File is required")
    private Long fileId;
}