package com.projects.EduConflux.config;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
public class DevDataSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDevAdmin() {

        return args -> {

            // Don't create the admin if it already exists
            if (userRepository.findByEmail("admin@educonflux.com").isPresent()) {
                return;
            }

            // Get an existing institution
            Institution institution =
                    institutionRepository.findAll()
                            .stream()
                            .findFirst()
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "No institution found. Create an institution first."
                                    ));

            // Get INSTITUTION_ADMIN role
            Role adminRole =
                    roleRepository.findByName("INSTITUTION_ADMIN")
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "INSTITUTION_ADMIN role not found"
                                    ));

            // Create admin
            User admin = User.builder()
                    .institution(institution)
                    .email("admin@educonflux.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .firstName("EduConflux")
                    .lastName("Admin")
                    .status(UserStatus.ACTIVE)
                    .build();

            User savedAdmin = userRepository.save(admin);

            // Assign role
            UserRole userRole = UserRole.builder()
                    .user(savedAdmin)
                    .role(adminRole)
                    .build();

            userRoleRepository.save(userRole);

            System.out.println(
                    "=========================================="
            );
            System.out.println(
                    "DEV ADMIN CREATED"
            );
            System.out.println(
                    "Email: admin@educonflux.com"
            );
            System.out.println(
                    "Password: Admin@123"
            );
            System.out.println(
                    "Institution: " + institution.getName()
            );
            System.out.println(
                    "=========================================="
            );
        };
    }
}