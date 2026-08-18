package com.projects.EduConflux.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    private static Authentication getAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }

        return authentication;
    }

    private static AuthenticatedUser getCurrentUser() {

        Object principal =
                getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticatedUser)) {

            throw new IllegalStateException(
                    "Authenticated user information not available"
            );
        }

        return (AuthenticatedUser) principal;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public static Long getCurrentInstitutionId() {
        return getCurrentUser().getInstitutionId();
    }

    public static java.util.List<String> getCurrentUserRoles() {
        return getCurrentUser().getRoles();
    }

    public static boolean hasRole(String role) {

        return getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_" + role)
                );
    }

    public static boolean isInstitutionAdmin() {
        return hasRole("INSTITUTION_ADMIN");
    }

    public static boolean isPlatformAdmin() {
        return hasRole("PLATFORM_ADMIN");
    }

    public static boolean isTeacher() {
        return hasRole("TEACHER");
    }

    public static boolean isStudent() {
        return hasRole("STUDENT");
    }

    public static boolean isParent() {
        return hasRole("PARENT");
    }
}