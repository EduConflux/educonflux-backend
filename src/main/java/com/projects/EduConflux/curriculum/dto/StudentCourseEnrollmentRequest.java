package com.projects.EduConflux.curriculum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentCourseEnrollmentRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course offering ID is required")
    private Long courseOfferingId;
}