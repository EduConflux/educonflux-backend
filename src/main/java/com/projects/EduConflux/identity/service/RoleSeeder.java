package com.projects.EduConflux.identity.service;

import com.projects.EduConflux.identity.entity.Role;
import com.projects.EduConflux.identity.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists(
                "PLATFORM_ADMIN",
                "Platform administrator with system-wide access"
        );

        createRoleIfNotExists(
                "INSTITUTION_ADMIN",
                "Administrator responsible for an institution"
        );

        createRoleIfNotExists(
                "TEACHER",
                "Teacher with academic and classroom access"
        );

        createRoleIfNotExists(
                "STUDENT",
                "Student with learning and academic access"
        );

        createRoleIfNotExists(
                "PARENT",
                "Parent with access to linked student information"
        );
    }

    private void createRoleIfNotExists(String name, String description) {

        if (!roleRepository.existsByName(name)) {

            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();

            roleRepository.save(role);
        }
    }
}