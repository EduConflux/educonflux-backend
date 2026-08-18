package com.projects.EduConflux.identity.service;

import com.projects.EduConflux.common.email.EmailService;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.identity.dto.ChangePasswordRequest;
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
    private final EmailService emailService;


    @Transactional
    public UserResponse createUser(UserRequest request) {

        // Get the currently authenticated admin's email
        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Institution institution =
                institutionRepository.findById(institutionId)
                        .orElseThrow(() ->
                                new RuntimeException("Institution not found"));


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

        String temporaryPassword = generateTemporaryPassword();

        String encodedPassword =
                passwordEncoder.encode(temporaryPassword);

        // Create the user
        User user = User.builder()
                .institution(institution)
                .email(request.getEmail())
                .personalEmail(request.getPersonalEmail())
                .password(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .profileImageUrl(request.getProfileImageUrl())
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        // Assign role
        UserRole userRole = UserRole.builder()
                .user(savedUser)
                .role(role)
                .build();

        userRoleRepository.save(userRole);

        emailService.sendCredentials(
                savedUser.getPersonalEmail(),
                savedUser.getEmail(),
                temporaryPassword
        );

        return toResponse(
                savedUser,
                List.of(role.getName())
        );
    }

    private UserResponse toResponse(
            User user,
            List<String> roles) {

        UserResponse response = new UserResponse();
        response.setFirstLogin(user.isFirstLogin());
        response.setId(user.getId());
        response.setInstitutionId(user.getInstitution().getId());
        response.setEmail(user.getEmail());
        response.setPersonalEmail(user.getPersonalEmail());
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

    private String generateTemporaryPassword() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
    }

    @Transactional
    public void changePassword(
            Long userId,
            ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Current password is incorrect"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        user.setFirstLogin(false);

        userRepository.save(user);
    }
}