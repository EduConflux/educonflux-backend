package com.projects.EduConflux.curriculum.dto;

import com.projects.EduConflux.curriculum.entity.StudentCourseEnrollmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentCourseEnrollmentResponse {

    private Long id;

    private Long studentId;
    private String enrollmentNumber;
    private String studentName;
    private String studentEmail;

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

    private StudentCourseEnrollmentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}