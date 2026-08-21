package com.projects.EduConflux.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentRequest {

    @NotNull(message = "Program ID is required")
    private Long programId;

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    private Long classSectionId;

    @NotBlank(message = "Enrollment number is required")
    @Size(
            max = 50,
            message = "Enrollment number cannot exceed 50 characters"
    )
    private String enrollmentNumber;

    @NotBlank(message = "First name is required")
    @Size(
            max = 100,
            message = "First name cannot exceed 100 characters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(
            max = 100,
            message = "Last name cannot exceed 100 characters"
    )
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    @Size(
            max = 150,
            message = "Email cannot exceed 150 characters"
    )
    private String email;

    @NotBlank(message = "Personal email is required")
    @Email(message = "Invalid personal email")
    @Size(
            max = 150,
            message = "Personal email cannot exceed 150 characters"
    )
    private String personalEmail;

    @Size(
            max = 20,
            message = "Phone cannot exceed 20 characters"
    )
    private String phone;

    private LocalDate admissionDate;
}