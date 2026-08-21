package com.projects.EduConflux.curriculum.dto;

import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FacultyCourseAssignmentResponse {

    private Long id;

    private Long facultyId;
    private String facultyName;
    private String facultyEmployeeId;
    private String facultyEmail;

    private Long courseOfferingId;

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

    private FacultyCourseAssignmentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}