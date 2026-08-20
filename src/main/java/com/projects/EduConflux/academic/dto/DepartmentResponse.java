package com.projects.EduConflux.academic.dto;

import com.projects.EduConflux.academic.entity.DepartmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepartmentResponse {

    private Long id;

    private Long institutionId;

    private String name;

    private String code;

    private String description;

    private DepartmentStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}