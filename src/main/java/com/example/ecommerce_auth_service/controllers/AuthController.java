package com.example.ecommerce_auth_service.controllers;

import com.example.ecommerce_auth_service.dtos.AuthResponseDto;
import com.example.ecommerce_auth_service.dtos.LoginRequestDto;
import com.example.ecommerce_auth_service.dtos.RegisterRequestDto;
import com.example.ecommerce_auth_service.models.Role;
import com.example.ecommerce_auth_service.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "200", description = "User registered successfully and JWT token returned")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "User already exists")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid RegisterRequestDto registerRequestDto) {

        if (registerRequestDto.getRole() == null) {
            registerRequestDto.setRole(Role.ROLE_USER);
        }

        AuthResponseDto response = authService.register(registerRequestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login user")
    @ApiResponse(responseCode = "200", description = "Login successful and JWT token returned")
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {

        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }
}