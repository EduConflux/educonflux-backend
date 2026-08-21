package com.projects.EduConflux.faculty.dto;

import com.projects.EduConflux.faculty.entity.FacultyStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FacultyResponse {

    private Long id;

    private Long institutionId;

    private Long departmentId;
    private String departmentName;

    private String employeeId;

    private String firstName;
    private String lastName;

    private String email;
    private String phone;

    private String designation;
    private String qualification;

    private LocalDate joiningDate;

    private FacultyStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}