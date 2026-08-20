package com.projects.EduConflux.academic.dto;

import com.projects.EduConflux.academic.entity.ProgramStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProgramResponse {

    private Long id;

    private Long institutionId;

    private Long departmentId;

    private String departmentName;

    private String name;

    private String code;

    private String description;

    private Integer durationYears;

    private ProgramStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}