package com.example.ecommerce_auth_service.advices;

import com.example.ecommerce_auth_service.dtos.ApiError;
import com.example.ecommerce_auth_service.exceptions.InvalidCredentialsException;
import com.example.ecommerce_auth_service.exceptions.InvalidRoleException;
import com.example.ecommerce_auth_service.exceptions.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ================= VALIDATION =================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                errors,
                "/api"
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {

        log.warn("Constraint violation: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                ex.getMessage(),
                "/api"
        );
    }

    // ================= AUTH / SECURITY =================

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {

        log.warn("Invalid credentials: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid Credentials",
                ex.getMessage(),
                "/auth"
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {

        log.warn("Bad credentials: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication Failed",
                "Invalid username or password",
                "/auth"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {

        log.warn("Access denied: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "You do not have permission to access this resource",
                "/api"
        );
    }

    // ================= BUSINESS EXCEPTIONS =================

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex) {

        log.warn("User already exists: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.CONFLICT,
                "User Already Exists",
                ex.getMessage(),
                "/auth"
        );
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ApiError> handleInvalidRole(InvalidRoleException ex) {

        log.warn("Invalid role: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Role",
                ex.getMessage(),
                "/auth"
        );
    }

    // ================= FALLBACK =================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {

        log.error("Unexpected error occurred", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Something went wrong",
                "/api"
        );
    }

    // ================= HELPER =================

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String error, String message, String path) {
        ApiError apiError = new ApiError(
                Instant.now(), status.value(),
                error, message, path
        );
        return new ResponseEntity<>(apiError, status);
    }
}