package com.projects.EduConflux.identity.dto;

import com.projects.EduConflux.identity.entity.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {

    private Long id;
    private Long institutionId;
    private String email;
    private String personalEmail;
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImageUrl;
    private UserStatus status;
    private List<String> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean firstLogin;
}