package com.projects.EduConflux.assignment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAssignmentRequest {

    @NotBlank(message = "Assignment title is required")
    @Size(
            max = 200,
            message = "Assignment title cannot exceed 200 characters"
    )
    private String title;

    @NotBlank(message = "Assignment description is required")
    @Size(
            max = 5000,
            message = "Assignment description cannot exceed 5000 characters"
    )
    private String description;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

    @NotNull(message = "Maximum marks are required")
    @Min(value = 1, message = "Maximum marks must be at least 1")
    @Max(value = 1000, message = "Maximum marks cannot exceed 1000")
    private Integer maxMarks;

    private Long fileId;
}