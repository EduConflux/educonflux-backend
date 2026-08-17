package com.projects.EduConflux.identity.service;

import com.projects.EduConflux.identity.dto.UserRequest;
import com.projects.EduConflux.identity.dto.UserResponse;
import com.projects.EduConflux.identity.entity.Role;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.entity.UserRole;
import com.projects.EduConflux.identity.entity.UserStatus;
import com.projects.EduConflux.identity.repository.RoleRepository;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.identity.repository.UserRoleRepository;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserRequest request) {

        String temporaryPassword =
                UUID.randomUUID().toString();

        // Get the currently authenticated admin's email
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String adminEmail = authentication.getName();

        // Find the admin
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new RuntimeException("Admin account not found"));

        // Make sure the admin actually belongs to an institution
        Institution institution = admin.getInstitution();

        // Find requested role
        Role role = roleRepository
                .findByName(request.getRole())
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        // Only allow normal institution-level roles to be created
        if (role.getName().equals("PLATFORM_ADMIN") ||
                role.getName().equals("INSTITUTION_ADMIN")) {

            throw new RuntimeException(
                    "This role cannot be created through this endpoint"
            );
        }

        // Create the user
        User user = User.builder()
                .institution(institution)
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .profileImageUrl(request.getProfileImageUrl())
                .status(UserStatus.PENDING)
                .build();

        User savedUser = userRepository.save(user);

        // Assign role
        UserRole userRole = UserRole.builder()
                .user(savedUser)
                .role(role)
                .build();

        userRoleRepository.save(userRole);

        return toResponse(
                savedUser,
                List.of(role.getName())
        );
    }

    private UserResponse toResponse(
            User user,
            List<String> roles) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setInstitutionId(user.getInstitution().getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setStatus(user.getStatus());
        response.setRoles(roles);
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}