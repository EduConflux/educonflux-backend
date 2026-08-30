package com.projects.EduConflux.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLearningMaterialRequest {

    @NotBlank(message = "Title is required")
    @Size(
            max = 200,
            message = "Title cannot exceed 200 characters"
    )
    private String title;


    @NotBlank(message = "Description is required")
    @Size(
            max = 10000,
            message = "Description cannot exceed 10000 characters"
    )
    private String description;


    /*
     * ID of an already uploaded file.
     *
     * Optional.
     */
    private Long fileId;
}