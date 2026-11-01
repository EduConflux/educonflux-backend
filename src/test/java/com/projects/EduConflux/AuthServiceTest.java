package com.projects.EduConflux;

import com.projects.EduConflux.identity.dto.LoginRequest;
import com.projects.EduConflux.identity.dto.LoginResponse;
import com.projects.EduConflux.identity.entity.Role;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.entity.UserRole;
import com.projects.EduConflux.identity.entity.UserStatus;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.identity.repository.UserRoleRepository;
import com.projects.EduConflux.identity.service.AuthService;
import com.projects.EduConflux.identity.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests corresponding to Section 5.2 (Table 5) of the Project Report:
 * - TC-01: User Login with valid credentials
 * - TC-02: User Login with invalid credentials
 * - TC-05 / TC-07: First-time user password reset enforcement (firstLogin flag)
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private Role teacherRole;
    private UserRole sampleUserRole;
    private LoginRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(101L)
                .email("prof.sharma@educonflux.ac.in")
                .password("$2a$10$hashedPasswordValue")
                .firstName("Rakesh")
                .lastName("Sharma")
                .personalEmail("rakesh.personal@gmail.com")
                .status(UserStatus.ACTIVE)
                .firstLogin(false)
                .build();

        teacherRole = Role.builder()
                .id(1L)
                .name("TEACHER")
                .description("Faculty member role")
                .build();

        sampleUserRole = UserRole.builder()
                .id(501L)
                .user(sampleUser)
                .role(teacherRole)
                .build();

        validRequest = new LoginRequest();
        validRequest.setEmail("prof.sharma@educonflux.ac.in");
        validRequest.setPassword("EduPass#2026");
    }

    // =========================================================================
    // TC-01: User Login with valid credentials
    // Expected: User authenticated, JWT generated, HTTP 200 equivalent response
    // =========================================================================
    @Test
    @DisplayName("TC-01: Valid credentials should successfully authenticate user and return signed JWT token")
    void testTC01_Login_Success_ValidCredentials() {
        // Arrange (Given)
        when(userRepository.findByEmail("prof.sharma@educonflux.ac.in"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("EduPass#2026", "$2a$10$hashedPasswordValue"))
                .thenReturn(true);
        when(userRoleRepository.findAll())
                .thenReturn(List.of(sampleUserRole));
        when(jwtService.generateToken(eq(sampleUser), anyList()))
                .thenReturn("mocked.header.payload.signature");

        // Act (When)
        LoginResponse response = authService.login(validRequest);

        // Assert (Then)
        assertNotNull(response, "Login response should not be null");
        assertEquals("mocked.header.payload.signature", response.getToken());
        assertEquals(101L, response.getUserId());
        assertEquals("prof.sharma@educonflux.ac.in", response.getEmail());
        assertTrue(response.getRoles().contains("TEACHER"), "Roles must include TEACHER");
        assertFalse(response.isFirstLogin());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail("prof.sharma@educonflux.ac.in");
        verify(passwordEncoder, times(1)).matches("EduPass#2026", "$2a$10$hashedPasswordValue");
        verify(jwtService, times(1)).generateToken(eq(sampleUser), anyList());
    }

    // =========================================================================
    // TC-02: User Login with invalid credentials (wrong password)
    // Expected: Authentication rejected with RuntimeException("Invalid email or password")
    // =========================================================================
    @Test
    @DisplayName("TC-02: Incorrect password must reject login and throw 'Invalid email or password'")
    void testTC02_Login_Failure_IncorrectPassword() {
        // Arrange (Given)
        LoginRequest badPasswordRequest = new LoginRequest();
        badPasswordRequest.setEmail("prof.sharma@educonflux.ac.in");
        badPasswordRequest.setPassword("WrongPassword#123");

        when(userRepository.findByEmail("prof.sharma@educonflux.ac.in"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword#123", "$2a$10$hashedPasswordValue"))
                .thenReturn(false);

        // Act & Assert (When & Then)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(badPasswordRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());

        // Verify JWT token is NEVER generated when authentication fails
        verify(jwtService, never()).generateToken(any(), anyList());
    }

    // =========================================================================
    // TC-02: User Login with non-existent user email
    // Expected: Authentication rejected with RuntimeException("Invalid email or password")
    // =========================================================================
    @Test
    @DisplayName("TC-02: Non-existent email must reject login and throw 'Invalid email or password'")
    void testTC02_Login_Failure_UserNotFound() {
        // Arrange (Given)
        LoginRequest unknownUserRequest = new LoginRequest();
        unknownUserRequest.setEmail("ghost@educonflux.ac.in");
        unknownUserRequest.setPassword("Secret#123");

        when(userRepository.findByEmail("ghost@educonflux.ac.in"))
                .thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(unknownUserRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(), anyList());
    }

    // =========================================================================
    // TC-02 (Extended): Inactive user account login rejection
    // Expected: Rejected with RuntimeException("User account is not active")
    // =========================================================================
    @Test
    @DisplayName("TC-02 (Extended): Suspended/Inactive user must be rejected with 'User account is not active'")
    void testLogin_Failure_InactiveAccount() {
        // Arrange
        sampleUser.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findByEmail("prof.sharma@educonflux.ac.in"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("EduPass#2026", "$2a$10$hashedPasswordValue"))
                .thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(validRequest);
        });

        assertEquals("User account is not active", exception.getMessage());
        verify(jwtService, never()).generateToken(any(), anyList());
    }

    // =========================================================================
    // TC-05: First-time user password reset enforcement (firstLogin flag = true)
    // Expected: Response contains firstLogin = true so frontend redirects to /change-password
    // =========================================================================
    @Test
    @DisplayName("TC-05: Newly provisioned user must have firstLogin=true returned in LoginResponse")
    void testTC05_Login_FirstTimeUser_FlagTrue() {
        // Arrange: Newly created user with default firstLogin = true
        sampleUser.setFirstLogin(true);

        when(userRepository.findByEmail("prof.sharma@educonflux.ac.in"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("EduPass#2026", "$2a$10$hashedPasswordValue"))
                .thenReturn(true);
        when(userRoleRepository.findAll())
                .thenReturn(List.of(sampleUserRole));
        when(jwtService.generateToken(eq(sampleUser), anyList()))
                .thenReturn("mocked.jwt.token");

        // Act
        LoginResponse response = authService.login(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isFirstLogin(), "firstLogin flag must be TRUE to prompt frontend redirect to password reset");
    }

    // =========================================================================
    // TC-05: Returning user after initial reset (firstLogin flag = false)
    // Expected: Response contains firstLogin = false so direct dashboard access is granted
    // =========================================================================
    @Test
    @DisplayName("TC-05: Returning user with password already set must have firstLogin=false")
    void testTC05_Login_ExistingUser_FlagFalse() {
        // Arrange: Existing user whose firstLogin is false
        sampleUser.setFirstLogin(false);

        when(userRepository.findByEmail("prof.sharma@educonflux.ac.in"))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("EduPass#2026", "$2a$10$hashedPasswordValue"))
                .thenReturn(true);
        when(userRoleRepository.findAll())
                .thenReturn(List.of(sampleUserRole));
        when(jwtService.generateToken(eq(sampleUser), anyList()))
                .thenReturn("mocked.jwt.token");

        // Act
        LoginResponse response = authService.login(validRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isFirstLogin(), "firstLogin flag must be FALSE for regular authenticated access");
    }
}
