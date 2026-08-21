package com.projects.EduConflux.curriculum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FacultyCourseAssignmentRequest {

    @NotNull(message = "Faculty ID is required")
    private Long facultyId;

    @NotNull(message = "Course offering ID is required")
    private Long courseOfferingId;
}