package com.projects.EduConflux.institution.dto;

import com.projects.EduConflux.institution.entity.InstitutionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InstitutionResponse {

    private Long id;
    private String name;
    private String code;
    private String email;
    private String phone;
    private String website;
    private String logoUrl;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private InstitutionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}