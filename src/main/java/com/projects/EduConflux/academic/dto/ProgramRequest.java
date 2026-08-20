package com.projects.EduConflux.academic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProgramRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Program name is required")
    @Size(
            max = 150,
            message = "Program name cannot exceed 150 characters"
    )
    private String name;

    @NotBlank(message = "Program code is required")
    @Size(
            max = 20,
            message = "Program code cannot exceed 20 characters"
    )
    private String code;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;

    @NotNull(message = "Duration is required")
    @Min(
            value = 1,
            message = "Duration must be at least 1 year"
    )
    @Max(
            value = 10,
            message = "Duration cannot exceed 10 years"
    )
    private Integer durationYears;
}