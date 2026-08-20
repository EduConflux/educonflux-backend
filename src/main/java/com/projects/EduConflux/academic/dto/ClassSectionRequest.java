package com.projects.EduConflux.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassSectionRequest {

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    @NotBlank(message = "Section name is required")
    @Size(
            max = 50,
            message = "Section name cannot exceed 50 characters"
    )
    private String name;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;
}