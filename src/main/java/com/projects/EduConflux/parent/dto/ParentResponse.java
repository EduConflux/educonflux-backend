package com.projects.EduConflux.parent.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParentResponse {

    private Long id;

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private String personalEmail;

    private String phone;

    private String relationship;

    private String occupation;

    private String address;

    private Long studentId;

    private String studentName;

    private String enrollmentNumber;
}
