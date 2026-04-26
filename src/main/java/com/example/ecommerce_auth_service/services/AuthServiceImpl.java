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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(
            UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, UserDetailsService userDetailsService,
            JwtService jwtService, AuthenticationManager authenticationManager,
            TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // ================= REGISTER =================
    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto dto) {

        String email = normalizeEmail(dto.getEmail());
        log.info("Register request received | email={}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed - user already exists | email={}", email);
            throw new UserAlreadyExistsException("User already exists with this email");
        }

        Set<Role> roles = resolveRoles(dto.getRoles(), email);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);

        log.info("User registered successfully | userId={} | email={}", user.getId(), email);

        UserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails, user.getId());
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpiration())
                .email(email)
                .build();
    }

    // ================= LOGIN =================
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto dto) {

        String email = normalizeEmail(dto.getEmail());
        log.info("Login attempt | email={}", email);

        User user;

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, dto.getPassword())
            );

            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        } catch (AuthenticationException ex) {
            log.warn("Login failed | email={} | reason={}", email, ex.getMessage());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Login successful | userId={} | email={}", user.getId(), email);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String accessToken = jwtService.generateToken(userDetails, user.getId());
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpiration())
                .email(email)
                .build();
    }

    // ================= REFRESH =================
    @Override
    public AuthResponseDto refreshToken(String refreshToken) {

        log.info("Refresh token request received");

        String email;

        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception ex) {
            log.warn("Invalid refresh token");
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, userDetails) || !jwtService.isRefreshToken(refreshToken)) {
            log.warn("Refresh token validation failed | email={}", email);
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        log.info("Refresh token valid | issuing new tokens | userId={}", user.getId());

        String newAccessToken = jwtService.generateToken(userDetails, user.getId());
        String newRefreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtService.getAccessTokenExpiration())
                .email(email)
                .build();
    }

    // ================= LOGOUT =================
    @Override
    public void logout(String accessToken) {

        log.info("Logout request received");

        if (!jwtService.isAccessToken(accessToken)) {
            log.warn("Invalid token type during logout");
            throw new InvalidCredentialsException("Invalid access token");
        }

        tokenBlacklistService.blacklist(
                accessToken,
                jwtService.extractExpiration(accessToken).getTime()
        );

        log.info("Logout successful - token blacklisted");

    }

    // ================= VALIDATE =================
    @Override
    public boolean validateToken(String token) {

        log.debug("Token validation request");

        if (tokenBlacklistService.isBlacklisted(token)) {
            log.warn("Token is blacklisted");
            return false;
        }

        try {
            String email = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            boolean isValid = jwtService.isTokenValid(token, userDetails);
            log.debug("Token validation result | email={} | valid={}", email, isValid);
            return isValid;
        } catch (Exception ex) {
            log.warn("Token validation failed | reason={}", ex.getMessage());
            return false;
        }

    }

    // ================= HELPERS =================

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private Set<Role> resolveRoles(Set<String> inputRoles, String email) {

        Set<String> roleNames;

        if (inputRoles == null || inputRoles.isEmpty()) {
            roleNames = Set.of("ROLE_USER");
        } else {
            roleNames = inputRoles.stream()
                    .map(role -> role.trim().toUpperCase())
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toSet());
        }

        if (roleNames.contains("ROLE_ADMIN")) {
            log.warn("Unauthorized admin role request during registration | email={}", email);
            throw new InvalidRoleException("Cannot register as admin");
        }

        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new InvalidRoleException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
    }
}