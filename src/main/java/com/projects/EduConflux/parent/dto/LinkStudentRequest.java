        package com.projects.EduConflux.parent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkStudentRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @Size(max = 30, message = "Relationship cannot exceed 30 characters")
    private String relationship;
}
