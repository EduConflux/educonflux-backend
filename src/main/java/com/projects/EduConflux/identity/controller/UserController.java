package com.projects.EduConflux.identity.controller;

import com.projects.EduConflux.identity.dto.UpdateUserRequest;
import com.projects.EduConflux.identity.dto.UserRequest;
import com.projects.EduConflux.identity.dto.UserResponse;
import com.projects.EduConflux.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(

            @Valid @RequestBody UserRequest request) {

        return ResponseEntity.ok(
                userService.createUser(request)
        );
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(

            @RequestParam(required = false)
            String role,

            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            String search,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sort,

            @RequestParam(defaultValue = "desc")
            String direction) {

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(sortDirection, sort)
                );

        return ResponseEntity.ok(
                userService.getUsers(
                        role,
                        status,
                        search,
                        pageable
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUser(userId)
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(
                userService.updateUser(userId, request)
        );
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long userId) {

        userService.deactivateUser(userId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<Void> activateUser(
            @PathVariable Long userId) {

        userService.activateUser(userId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id) {

        userService.resetPassword(id);

        return ResponseEntity.noContent().build();
    }
}