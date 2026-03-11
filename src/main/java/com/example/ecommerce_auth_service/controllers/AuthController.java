package com.example.ecommerce_auth_service.controllers;

import com.example.ecommerce_auth_service.dtos.AuthResponseDto;
import com.example.ecommerce_auth_service.dtos.LoginRequestDto;
import com.example.ecommerce_auth_service.dtos.RegisterRequestDto;
import com.example.ecommerce_auth_service.models.Role;
import com.example.ecommerce_auth_service.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register a new user with role (USER or ADMIN)
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid RegisterRequestDto registerRequestDto) {

        // Default role to USER if not provided
        if (registerRequestDto.getRole() == null) {
            registerRequestDto.setRole(Role.USER);
        }

        AuthResponseDto response = authService.register(registerRequestDto);
        return ResponseEntity.ok(response);
    }

    // Login existing user and get JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }
}