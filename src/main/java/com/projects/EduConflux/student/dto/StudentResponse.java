package com.projects.EduConflux.student.dto;

import com.projects.EduConflux.student.entity.StudentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentResponse {

    private Long id;

    private Long institutionId;

    private Long programId;
    private String programName;

    private Long classSectionId;
    private String classSectionName;

    private Long academicYearId;
    private String academicYearName;

    private String enrollmentNumber;

    private String firstName;
    private String lastName;

    private String email;
    private String personalEmail;

    private String phone;

    private LocalDate admissionDate;

    private StudentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}