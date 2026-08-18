package com.projects.EduConflux.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {

    private Long userId;
    private String email;
    private Long institutionId;
    private List<String> roles;
}