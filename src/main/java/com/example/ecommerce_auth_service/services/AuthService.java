package com.example.ecommerce_auth_service.services;

import com.example.ecommerce_auth_service.dtos.AuthResponseDto;
import com.example.ecommerce_auth_service.dtos.LoginRequestDto;
import com.example.ecommerce_auth_service.dtos.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto registerRequestDto);
    AuthResponseDto login(LoginRequestDto loginRequestDto);
}