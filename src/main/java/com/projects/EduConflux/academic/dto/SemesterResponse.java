package com.projects.EduConflux.academic.dto;

import com.projects.EduConflux.academic.entity.SemesterStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SemesterResponse {

    private Long id;

    private Long institutionId;

    private Long programId;

    private String programName;

    private Integer semesterNumber;

    private String name;

    private SemesterStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}