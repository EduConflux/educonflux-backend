package com.projects.EduConflux.learning.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningMaterialResponse {

    private Long id;

    private Long classroomId;

    private String classroomName;

    private Long facultyId;

    private String facultyName;

    private String title;

    private String description;

    private Long fileId;

    private String fileName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}