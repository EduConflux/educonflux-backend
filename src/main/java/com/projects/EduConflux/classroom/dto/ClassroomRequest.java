package com.projects.EduConflux.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomRequest {

    @NotBlank(message = "Classroom name is required")
    @Size(max = 200, message = "Classroom name cannot exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Course offering is required")
    private Long courseOfferingId;

    @NotNull(message = "Class section is required")
    private Long classSectionId;
}