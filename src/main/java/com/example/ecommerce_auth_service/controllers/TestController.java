package com.example.ecommerce_auth_service.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@Tag(name = "Test APIs", description = "Endpoints to test role-based access control")
public class TestController {

    @Operation(
            summary = "Admin-only endpoint",
            description = "Accessible only by users with ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Admin access granted";
    }

    @Operation(
            summary = "User-only endpoint",
            description = "Accessible only by users with USER role",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('USER')")
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