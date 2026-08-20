package com.projects.EduConflux.academic.dto;

import com.projects.EduConflux.academic.entity.CourseType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Program ID is required")
    private Long programId;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    @NotBlank(message = "Course code is required")
    @Size(
            max = 20,
            message = "Course code cannot exceed 20 characters"
    )
    private String code;

    @NotBlank(message = "Course name is required")
    @Size(
            max = 150,
            message = "Course name cannot exceed 150 characters"
    )
    private String name;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;

    @NotNull(message = "Credits are required")
    @Min(
            value = 1,
            message = "Credits must be at least 1"
    )
    @Max(
            value = 20,
            message = "Credits cannot exceed 20"
    )
    private Integer credits;

    @NotNull(message = "Course type is required")
    private CourseType courseType;
}