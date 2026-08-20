package com.projects.EduConflux.config;

import com.projects.EduConflux.identity.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints
                        .requestMatchers("/api/auth/login")
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
}