package com.example.ecommerce_auth_service.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Admin access granted";
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user")
    public String userEndpoint() {
        return "User access granted";
    }
}


// Built a secure Authentication & Authorization microservice using Spring Boot, Spring Security, JWT, Refresh Tokens, Role-Based Access Control,
// and Token Blacklisting, with global exception handling, Swagger documentation, and unit-tested service layer.

// Implemented:
//JWT Authentication with access + refresh tokens
//Role-based Authorization (USER / ADMIN)
//Spring Security stateless config
//Custom JWT Filter
//Logout via Token Blacklisting
//Custom AuthenticationEntryPoint / AccessDeniedHandler
//Global Exception Handling
//Validation with DTOs
//Unit Tests with Mockito/JUnit 5
//Swagger/OpenAPI Documentation
//Repository + Service + Controller layering
//Proper separation of concerns