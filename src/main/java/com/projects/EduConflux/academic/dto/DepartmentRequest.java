package com.projects.EduConflux.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(
            max = 150,
            message = "Department name cannot exceed 150 characters"
    )
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(
            max = 20,
            message = "Department code cannot exceed 20 characters"
    )
    private String code;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;
}