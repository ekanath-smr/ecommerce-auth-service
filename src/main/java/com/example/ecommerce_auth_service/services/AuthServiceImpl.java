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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
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

        logger.info("Register request received for email: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists with this email");
        }

        Set<Role> roles = resolveRoles(dto.getRoles(), email);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);

//        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
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
        User user;
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, dto.getPassword())
            );
            user = userRepository.findByEmail(dto.getEmail()).get();
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//        UserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails, user.getId());
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpiration())
                .email(email)
                .build();
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken) {

        String email;

        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception ex) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User user = userRepository.findByEmail(email).get();

        if (!jwtService.isTokenValid(refreshToken, userDetails) || !jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateToken(userDetails, user.getId());
        String newRefreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken) // optional: reuse old refresh token
                .expiresIn(jwtService.getAccessTokenExpiration())
                .email(email)
                .build();
    }

    @Override
    public void logout(String accessToken) {

        logger.info("Logout request received");

        if (!jwtService.isAccessToken(accessToken)) {
            throw new InvalidCredentialsException("Invalid access token");
        }

        tokenBlacklistService.blacklist(accessToken);

        logger.info("Token blacklisted successfully");
    }

    @Override
    public boolean validateToken(String token) {

        logger.debug("Token validation request received");

        if (tokenBlacklistService.isBlacklisted(token)) {
            logger.warn("Token is blacklisted");
            return false;
        }

        try {
            String email = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception ex) {
            logger.warn("Token validation failed");
            return false;
        }
    }

    // ================= HELPER METHODS =================

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private Set<Role> resolveRoles(Set<String> inputRoles, String email) throws InvalidRoleException {

        Set<String> roleNames;

        if (inputRoles == null || inputRoles.isEmpty()) {
            roleNames = Set.of("ROLE_USER");
        } else {
            roleNames = inputRoles.stream()
                    .map(role -> role.trim().toUpperCase())
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toSet());
        }

        // Prevent privilege escalation
        if (roleNames.contains("ROLE_ADMIN")) {
            logger.warn("Unauthorized admin registration attempt: {}", email);
            throw new InvalidRoleException("Cannot register as admin");
        }

        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new InvalidRoleException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
    }
}