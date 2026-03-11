package com.example.ecommerce_auth_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/admin/test")
    public String adminEndpoint() {
        return "Admin access granted";
    }

    @GetMapping("/user/test")
    public String userEndpoint() {
        return "User access granted";
    }
}