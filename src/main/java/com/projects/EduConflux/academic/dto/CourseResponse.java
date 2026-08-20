package com.projects.EduConflux.academic.dto;

import com.projects.EduConflux.academic.entity.CourseStatus;
import com.projects.EduConflux.academic.entity.CourseType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseResponse {

    private Long id;

    private Long institutionId;

    private Long departmentId;
    private String departmentName;

    private Long programId;
    private String programName;

    private Long semesterId;
    private String semesterName;
    private Integer semesterNumber;

    private String code;
    private String name;
    private String description;

    private Integer credits;
    private CourseType courseType;

    private CourseStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}