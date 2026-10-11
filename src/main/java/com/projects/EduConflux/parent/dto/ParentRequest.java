package com.projects.EduConflux.parent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Parent's institutional email is required")
    @Email(message = "Enter a valid institutional email")
    private String email;

    @NotBlank(message = "Parent's personal email is required")
    @Email(message = "Enter a valid personal email")
    private String personalEmail;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;

    private String relationship;

    private String occupation;

    private String address;
}
