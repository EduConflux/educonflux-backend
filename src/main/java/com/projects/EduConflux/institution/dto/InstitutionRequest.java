package com.projects.EduConflux.institution.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionRequest {

    @NotBlank(message = "Institution name is required")
    @Size(max = 150, message = "Institution name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Institution code is required")
    @Size(max = 50, message = "Institution code cannot exceed 50 characters")
    private String code;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @Pattern(
            regexp = "^[0-9+()\\- ]{7,20}$",
            message = "Invalid phone number"
    )
    private String phone;

    private String website;

    private String logoUrl;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @Pattern(
            regexp = "^[0-9A-Za-z -]{3,10}$",
            message = "Invalid postal code"
    )
    private String postalCode;
}