package com.projects.EduConflux.config;

import com.projects.EduConflux.identity.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${educonflux.frontend.url:https://edu-conflux.vercel.app}")
    private String configuredFrontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Public health check endpoints
                        .requestMatchers("/api/health", "/health")
                        .permitAll()

                        // Public authentication endpoints
                        .requestMatchers("/api/auth/login")
                        .permitAll()

                        .requestMatchers("/ws/**")
                        .permitAll()

                        // Authenticated users only
                        .requestMatchers("/api/auth/change-password")
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/admin/users/**"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/admin/users/**"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/admin/users/*/deactivate"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/admin/users/*/activate"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        // Institution admin only
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/admin/users"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers("/api/institutions/**")
                        .hasAnyRole("PLATFORM_ADMIN", "INSTITUTION_ADMIN")

                        .requestMatchers("/api/admin/academic-years/**")
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers("/api/admin/departments/**")
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers("/api/admin/programs/**")
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers("/api/admin/semesters/**")
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers("/api/admin/class-sections/**")
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers("/api/admin/courses/**")
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                "/api/admin/faculty",
                                "/api/admin/faculty/**"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers("/api/admin/students/**")
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                "/api/admin/course-offerings/**"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                "/api/admin/faculty-course-assignments/**"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                "/api/admin/student-course-enrollments/**"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                "/api/admin/timetable/**"
                        )
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers(
                                "/api/student/timetable/**"
                        )
                        .hasRole("STUDENT")

                        .requestMatchers(
                                "/api/faculty/timetable/**"
                        )
                        .hasRole("FACULTY")

                        .requestMatchers(
                                "/api/faculty/attendance/**"
                        )
                        .hasRole("FACULTY")

                        .requestMatchers(
                                "/api/student/attendance/**"
                        )
                        .hasRole("STUDENT")

                        .requestMatchers("/api/faculty/classrooms/**")
                        .hasRole("FACULTY")

                        .requestMatchers(
                                "/api/faculty/classrooms/*/invitations/**"
                        )
                        .hasRole("FACULTY")

                        .requestMatchers(
                                "/api/student/classrooms/join"
                        )
                        .hasRole("STUDENT")

                        .requestMatchers("/api/student/classrooms/**")
                        .hasRole("STUDENT")

                        .requestMatchers("/api/faculty/classrooms/**")
                        .hasRole("FACULTY")

                        .requestMatchers("/api/student/classrooms/**")
                        .hasRole("STUDENT")

                        .requestMatchers(
                                "/api/student/notifications/**"
                        )
                        .hasRole("STUDENT")

                        .requestMatchers(
                                "/api/faculty/notifications/**"
                        )
                        .hasRole("FACULTY")

                        .requestMatchers("/api/notifications/**")
                        .hasAnyRole("STUDENT", "FACULTY", "INSTITUTION_ADMIN")

                        .requestMatchers(
                                "/api/faculty/classrooms/*/learning-materials"
                        )
                        .hasRole("FACULTY")

                        .requestMatchers(
                                "/api/student/classrooms/*/learning-materials"
                        )
                        .hasRole("STUDENT")

                        .requestMatchers(
                                "/api/faculty/classrooms/*/learning-materials"
                        )
                        .hasRole("FACULTY")

                        .requestMatchers(
                                "/api/faculty/learning-materials/**"
                        )
                        .hasRole("FACULTY")

                        .requestMatchers("/api/parents/me/**")
                        .hasRole("PARENT")

                        .requestMatchers("/api/parents/**")
                        .hasRole("INSTITUTION_ADMIN")

                        .requestMatchers("/api/search")
                        .authenticated()


                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        java.util.List<String> allowedOrigins = new java.util.ArrayList<>(java.util.List.of(
                "https://edu-conflux.vercel.app",
                "https://edu-conflux-*.vercel.app",
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:4173"
        ));

        if (configuredFrontendUrl != null && !configuredFrontendUrl.isBlank()) {
            String sanitized = configuredFrontendUrl.trim().replaceAll("/+$", "");
            if (!allowedOrigins.contains(sanitized)) {
                allowedOrigins.add(sanitized);
            }
        }

        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setExposedHeaders(java.util.List.of("Authorization", "Content-Disposition"));
        configuration.setMaxAge(3600L);
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}