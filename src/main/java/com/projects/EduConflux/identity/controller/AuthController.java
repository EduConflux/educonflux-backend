package com.projects.EduConflux.identity.controller;

import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.identity.dto.ChangePasswordRequest;
import com.projects.EduConflux.identity.dto.LoginRequest;
import com.projects.EduConflux.identity.dto.LoginResponse;
import com.projects.EduConflux.identity.service.AuthService;
import com.projects.EduConflux.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.projects.EduConflux.identity.dto.ChangePasswordRequest;
import com.projects.EduConflux.common.security.SecurityUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();

        userService.changePassword(userId, request);

        return ResponseEntity.ok().build();
    }
}