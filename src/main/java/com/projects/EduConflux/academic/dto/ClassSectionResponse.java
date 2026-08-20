package com.projects.EduConflux.academic.dto;

import com.projects.EduConflux.academic.entity.ClassSectionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassSectionResponse {

    private Long id;

    private Long institutionId;

    private Long semesterId;

    private String semesterName;

    private Integer semesterNumber;

    private Long programId;

    private String programName;

    private String name;

    private String description;

    private ClassSectionStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}