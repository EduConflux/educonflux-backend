package com.projects.EduConflux.identity.service;

import com.projects.EduConflux.identity.dto.LoginRequest;
import com.projects.EduConflux.identity.dto.LoginResponse;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.entity.UserRole;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.identity.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        if (user.getStatus() != com.projects.EduConflux.identity.entity.UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }

        List<String> roles = userRoleRepository
                .findAll()
                .stream()
                .filter(userRole ->
                        userRole.getUser().getId().equals(user.getId()))
                .map(UserRole::getRole)
                .map(role -> role.getName())
                .toList();

        String token = jwtService.generateToken(user, roles);

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                roles
        );
    }
}