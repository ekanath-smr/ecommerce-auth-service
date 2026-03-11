package com.example.ecommerce_auth_service.services;

import com.example.ecommerce_auth_service.dtos.AuthResponseDto;
import com.example.ecommerce_auth_service.dtos.LoginRequestDto;
import com.example.ecommerce_auth_service.dtos.RegisterRequestDto;
import com.example.ecommerce_auth_service.exceptions.InvalidCredentialsException;
import com.example.ecommerce_auth_service.exceptions.UserAlreadyExistsException;
import com.example.ecommerce_auth_service.models.Role;
import com.example.ecommerce_auth_service.models.User;
import com.example.ecommerce_auth_service.repositories.UserRepository;
import com.example.ecommerce_auth_service.security.CustomUserDetails;
import com.example.ecommerce_auth_service.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponseDto register(RegisterRequestDto registerRequestDto) {

        logger.info("Register request received for email: {}", registerRequestDto.getEmail());

        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            logger.warn("Registration failed. User already exists with email: {}", registerRequestDto.getEmail());
            throw new UserAlreadyExistsException("User already exists with this email");
        }

        // Prevent users from registering as ROLE_ADMIN
        if (registerRequestDto.getRole() == Role.ROLE_ADMIN) {
            logger.warn("Attempt to register with ROLE_ADMIN role for email: {}", registerRequestDto.getEmail());
            throw new RuntimeException("Cannot register as admin");
        }

        User user = User.builder()
                .email(registerRequestDto.getEmail())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .role(registerRequestDto.getRole())
                .build();

        userRepository.save(user);

        logger.info("User registered successfully with email: {}", user.getEmail());

        // Use CustomUserDetails for role-based JWT
        UserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        logger.debug("JWT token generated for user: {}", user.getEmail());

        return new AuthResponseDto(token);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {

        logger.info("Login attempt for email: {}", loginRequestDto.getEmail());

        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed. User not found for email: {}", loginRequestDto.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            logger.warn("Login failed due to invalid password for email: {}", loginRequestDto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        logger.info("User authenticated successfully: {}", user.getEmail());

        // Use CustomUserDetails for role-based JWT
        UserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        logger.debug("JWT token generated for user: {}", user.getEmail());

        return new AuthResponseDto(token);
    }
}