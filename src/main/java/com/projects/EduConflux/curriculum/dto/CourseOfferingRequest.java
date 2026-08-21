package com.projects.EduConflux.curriculum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseOfferingRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;
}