package com.projects.EduConflux.identity.controller;

import com.projects.EduConflux.identity.dto.ActivationRequest;
import com.projects.EduConflux.identity.service.ActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ActivationController {

    private final ActivationService activationService;

    @PostMapping("/activate")
    public ResponseEntity<String> activate(
            @Valid @RequestBody ActivationRequest request) {

        activationService.activateAccount(request.getToken());

        return ResponseEntity.ok(
                "Account activated successfully"
        );
    }
}