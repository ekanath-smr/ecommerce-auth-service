package com.example.ecommerce_auth_service.advices;

import com.example.ecommerce_auth_service.dtos.ApiError;
import com.example.ecommerce_auth_service.exceptions.InvalidCredentialsException;
import com.example.ecommerce_auth_service.exceptions.InvalidRoleException;
import com.example.ecommerce_auth_service.exceptions.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation error at {} : {}", request.getRequestURI(), errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST, "Validation Failed",
                errors, request.getRequestURI()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Constraint violation at {} : {}", request.getRequestURI(), ex.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST, "Constraint Violation",
                ex.getMessage(), request.getRequestURI()
        );
    }

    // ================= AUTH / SECURITY =================

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {

        log.warn("Invalid credentials at {} : {}", request.getRequestURI(), ex.getMessage());

        return buildResponse(
                HttpStatus.UNAUTHORIZED, "Invalid Credentials",
                ex.getMessage(), request.getRequestURI()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Authentication failed at {} : {}", request.getRequestURI(), ex.getMessage());

        return buildResponse(
                HttpStatus.UNAUTHORIZED, "Authentication Failed",
                "Invalid username or password", request.getRequestURI()
        );
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiError> handleAccessDenied(Exception ex) {
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
    public ResponseEntity<ApiError> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {

        log.warn("User already exists at {} : {}", request.getRequestURI(), ex.getMessage());

        return buildResponse(
                HttpStatus.CONFLICT, "User Already Exists",
                ex.getMessage(), request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ApiError> handleInvalidRole(
            InvalidRoleException ex, HttpServletRequest request) {

        log.warn("Invalid role at {} : {}", request.getRequestURI(), ex.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST, "Invalid Role",
                ex.getMessage(), request.getRequestURI()
        );
    }

    // ================= FALLBACK =================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}", request.getRequestURI(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Something went wrong", request.getRequestURI()
        );
    }

    // ================= HELPER =================

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status, String error, String message, String path) {
        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                error,
                message,
                path
        );
        return new ResponseEntity<>(apiError, status);
    }
}