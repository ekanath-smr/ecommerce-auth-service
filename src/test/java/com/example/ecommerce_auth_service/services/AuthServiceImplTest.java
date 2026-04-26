package com.example.ecommerce_auth_service.services;

import com.example.ecommerce_auth_service.dtos.AuthResponseDto;
import com.example.ecommerce_auth_service.dtos.LoginRequestDto;
import com.example.ecommerce_auth_service.dtos.RegisterRequestDto;
import com.example.ecommerce_auth_service.exceptions.InvalidCredentialsException;
import com.example.ecommerce_auth_service.exceptions.InvalidRoleException;
import com.example.ecommerce_auth_service.exceptions.UserAlreadyExistsException;
import com.example.ecommerce_auth_service.models.Role;
import com.example.ecommerce_auth_service.models.User;
import com.example.ecommerce_auth_service.repositories.RoleRepository;
import com.example.ecommerce_auth_service.repositories.UserRepository;
import com.example.ecommerce_auth_service.security.CustomUserDetails;
import com.example.ecommerce_auth_service.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserDetailsService userDetailsService;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    // ================= REGISTER =================

    @Test
    @DisplayName("Should register user successfully when valid data is provided")
    void register_shouldRegisterUserSuccessfully() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("TEST@EXAMPLE.COM");
        request.setPassword("password123");
        request.setRoles(Set.of("USER"));

        Role userRole = new Role("ROLE_USER");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);

        AuthResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("test@example.com") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRoles().size() == 1
        ));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email already exists")
    void register_shouldThrowException_whenUserAlreadyExists() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidRoleException when invalid role is provided")
    void register_shouldThrowException_whenInvalidRoleProvided() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setRoles(Set.of("INVALID_ROLE"));

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_INVALID_ROLE"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRoleException.class,
                () -> authService.register(request));
    }

    // ================= LOGIN =================

    @Test
    @DisplayName("Should login successfully when credentials are valid")
    void login_shouldLoginSuccessfully() {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .roles(Set.of(new Role("ROLE_USER")))
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(new CustomUserDetails(user));

//        when(authenticationManager.authenticate(any()))
//                .thenReturn(null);
        when(authenticationManager.authenticate(any()))
                .thenReturn(mock(Authentication.class));

        when(jwtService.generateToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());

        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when authentication fails")
    void login_shouldThrowException_whenAuthenticationFails() {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }

    // ================= LOGOUT =================

    @Test
    @DisplayName("Should blacklist token successfully during logout")
    void logout_shouldBlacklistToken() {

        String token = "jwt-token";

        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(jwtService.extractExpiration(token)).thenReturn(new Date(1000));

        authService.logout(token);

        verify(tokenBlacklistService)
                .blacklist(eq(token), eq(1000L));
    }

    // ================= VALIDATION =================

    @Test
    @DisplayName("Should return false when token is blacklisted")
    void validateToken_shouldReturnFalse_whenBlacklisted() {

        String token = "blacklisted-token";

        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        boolean valid = authService.validateToken(token);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should return true when token is valid")
    void validateToken_shouldReturnTrue_whenValid() {

        String token = "valid-token";

        User user = User.builder()
                .email("test@example.com")
                .password("pass")
                .roles(Set.of(new Role("ROLE_USER")))
                .build();

        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
        when(jwtService.extractUsername(token)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(new CustomUserDetails(user));
        when(jwtService.isTokenValid(eq(token), any())).thenReturn(true);

        boolean result = authService.validateToken(token);

        assertTrue(result);
    }
}