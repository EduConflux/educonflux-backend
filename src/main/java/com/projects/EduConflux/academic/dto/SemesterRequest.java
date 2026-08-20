package com.projects.EduConflux.academic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SemesterRequest {

    @NotNull(message = "Program ID is required")
    private Long programId;

    @NotNull(message = "Semester number is required")
    @Min(
            value = 1,
            message = "Semester number must be at least 1"
    )
    @Max(
            value = 20,
            message = "Semester number cannot exceed 20"
    )
    private Integer semesterNumber;

    @NotBlank(message = "Semester name is required")
    @Size(
            max = 100,
            message = "Semester name cannot exceed 100 characters"
    )
    private String name;
}