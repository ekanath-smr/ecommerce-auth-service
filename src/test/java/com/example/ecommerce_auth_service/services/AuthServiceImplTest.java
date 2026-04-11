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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should register user successfully when valid data is provided")
    void register_shouldRegisterUserSuccessfully() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRoles(Set.of("USER"));

        Role userRole = new Role("ROLE_USER");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);

        AuthResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email already exists during registration")
    void register_shouldThrowException_whenUserAlreadyExists() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidRoleException when invalid role is provided during registration")
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

    @Test
    @DisplayName("Should login successfully when credentials are valid")
    void login_shouldLoginSuccessfully() {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .roles(Set.of(
                        new Role("ROLE_USER")
                ))
                .build();

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(new CustomUserDetails(user));

        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when authentication fails during login")
    void login_shouldThrowException_whenAuthenticationFails() {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    @DisplayName("Should blacklist token successfully during logout")
    void logout_shouldBlacklistToken() {

        String token = "jwt-token";

        authService.logout(token);

        verify(tokenBlacklistService, times(1))
                .blacklist(token);
    }

    @Test
    @DisplayName("Should return false when validating a blacklisted token")
    void validateToken_shouldReturnFalse_whenBlacklisted() {

        String token = "blacklisted-token";

        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        boolean valid = authService.validateToken(token);

        assertFalse(valid);
    }
}