package com.example.ecommerce_auth_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Admin access granted";
    }

    @GetMapping("/user")
    public String userEndpoint() {
        return "User access granted";
    }
}