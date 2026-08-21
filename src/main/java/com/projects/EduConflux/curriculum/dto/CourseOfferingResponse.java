package com.projects.EduConflux.curriculum.dto;

import com.projects.EduConflux.curriculum.entity.CourseOfferingStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseOfferingResponse {

    private Long id;

    private Long institutionId;

    private Long courseId;
    private String courseCode;
    private String courseName;

    private Long programId;
    private String programName;

    private Long semesterId;
    private Integer semesterNumber;
    private String semesterName;

    private Long academicYearId;
    private String academicYearName;

    private CourseOfferingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}