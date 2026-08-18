package com.projects.EduConflux.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long userId;
    private String email;
    private List<String> roles;
    private boolean firstLogin;
}