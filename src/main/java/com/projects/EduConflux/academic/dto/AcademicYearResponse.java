package com.projects.EduConflux.academic.dto;

import com.projects.EduConflux.academic.entity.AcademicYearStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AcademicYearResponse {

    private Long id;

    private Long institutionId;

    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private AcademicYearStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}